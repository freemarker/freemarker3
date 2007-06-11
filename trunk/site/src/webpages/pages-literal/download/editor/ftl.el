;;; ftl.el --- fontify velocity template language code

;; Modified from vtl mode by Brian Leonard <brian@brainslug.org>
;; Maintainer: marvin.greenberg@acm.org
;; Keywords: extensions
;; Created: 2003-03-13
;; Version 0.1

;;; Commentary:

;;;
;;; Known bugs!:
;;;
;;; Hilighting of strings with escaped quotes will be erroneously terminated at the escaped quote.
;;; Strings are fontified everywhere, not just in interpolations and directives
;;; An occurrence of '>' within a string or expression in a directive or interpolation
;;;   will incorrectly terminate highlighting.
;;; An occurrence of '}' within a string argument to a method in an interpolation
;;;   will incorrectly terminate highlighting for the interpolation.
;;;

;; One useful way to enable this minor mode is to put the following in your
;; .emacs, assuming you save this in your home directory also:
;;      (load-file "~/ftl.el")
;;      (autoload 'turn-on-ftl-mode "ftl" nil t)
;;      (add-hook 'html-mode-hook 'turn-on-ftl-mode t t)
;;      (add-hook 'xml-mode-hook 'turn-on-ftl-mode t t)
;;      (add-hook 'text-mode-hook 'turn-on-ftl-mode t t)
;;
;;  Also this might be useful
;;
;;   (setq auto-mode-alist (cons (cons "\\.ftl$" 'ftl-mode) auto-mode-alist))

;;; Code:

(require 'font-lock)
(require 'cl)

(defgroup ftl nil
  "Fontifies FTL code.  see http://freemarker.org"
  :group 'ftl
  :group 'font-lock
  :group 'extensions)

;;;###autoload
(defcustom ftl-mode nil
  "*If non-nil, fontify ftl code"
  :type 'boolean) 
(make-variable-buffer-local 'ftl-mode) 

;;;###autoload
(defcustom ftl-minor-mode-string " FTL"
  "*String to display in mode line when FTL Mode is enabled."
  :type 'string
  :group 'ftl)

;;;###autoload
(defun turn-on-ftl-mode ()
  "Unequivocally turn on ftl-mode (see variable documentation)."
  (interactive)
  (font-lock-mode 1)
  (ftl-mode 1))

;; Put minor mode string on the global minor-mode-alist.
;;;###autoload
(cond ((fboundp 'add-minor-mode)
       (add-minor-mode 'ftl-mode 'ftl-minor-mode-string))
      ((assq 'ftl-mode (default-value 'minor-mode-alist)))
      (t
       (setq-default minor-mode-alist
                     (append (default-value 'minor-mode-alist)
                             '((ftl-mode ftl-minor-mode-string))))))

;;;###autoload
(defun ftl-mode (&optional prefix)
  "Toggle FTL Mode.

If called interactively with no prefix argument, toggle current condition
of the mode.
If called with a positive or negative prefix argument, enable or disable
the mode, respectively."
  (interactive "P")

  (setq ftl-mode
	(if prefix
	    (>= (prefix-numeric-value prefix) 0)
	  (not ftl-mode)))

  (cond (ftl-mode
	 ;; first, grab default
	 (font-lock-mode 0)
	 (font-lock-set-defaults)

	 ;; add ftl regexps
	 (setq font-lock-keywords
	       (let ((new-keywords
		      (cond ((null font-lock-keywords)
			     ftl-keywords)
			    (t
			     (list* (car font-lock-keywords)
				    (append (cdr font-lock-keywords)
					    ftl-keywords))))))
		 new-keywords))

	 ;; and restart font-lock
	 (font-lock-mode 1)
	 (font-lock-fontify-buffer))

	(t
	 ;; reset to major mode's defaults
	 (font-lock-mode 0)
	 (font-lock-set-defaults)
	 (font-lock-mode 1)
	 (font-lock-fontify-buffer)))
	 
  (and (interactive-p)
       (if ftl-mode
           (message "ftl-mode is enabled")
         (message "ftl-mode is disabled")))
  ftl-mode)


;
; tried complex, now try simple

(setq ftl-keywords
  (let 
      (
       (directive (concat "[<][/]?#\\(assign\\|if\\|elseif\\|else\\|foreach\\|"
                          "list\\|break\\|import\\|include\\|noparse\\|compress\\|"
                          "escape\\|noescape\\|global\\|local\\|setting\\|"
                          "switch\\|case\\|call\\|break\\|"
			  "nested\\|return\\|flush\\|stop\\|macro\\|ftl\\|"
                          "t\\|lt\\|rt\\)"
                          "[^a-zA-Z][^>]*[>]"))
       (directive-noargs (concat "[<][/]?#\\(assign\\|if\\|elseif\\|else\\|foreach\\|"
                          "list\\|break\\|import\\|include\\|noparse\\|compress\\|"
                          "escape\\|noescape\\|global\\|local\\|setting\\|"
                          "switch\\|case\\|call\\|break\\|"
			  "nested\\|return\\|flush\\|stop\\|macro\\|ftl\\|"
                          "t\\|lt\\|rt\\)"
                          "[>]"))
       (invalid-directive "\\([<][/]?#[a-zA-Z][a-zA-Z_0-9]*\\)[^>]*\\([>]\\)")
       (user-directive "[<][/]?@[a-zA-Z_][a-zA-Z0-9_]*[^>]*[>]")
       (interpolation-all  "[#$][{][^}]+[}]")
       (string  "[\"][^\"]*[\"]")
       (sq-string  "[\'][^\']*[\']")
       (comment "[<]#--[^>]*--[>]"))

    (list
     (list user-directive '(0 font-lock-function-name-face t))
     (list invalid-directive '(1 font-lock-warning-face t))
     (list invalid-directive '(2 font-lock-warning-face t))
     (list directive '(0 font-lock-keyword-face t))
     (list directive-noargs '(0 font-lock-keyword-face t))
     (list interpolation-all '(0 font-lock-type-face t))
     (list string    '(0 font-lock-string-face t))
     (list sq-string    '(0 font-lock-string-face t))
     (list comment   '(0 font-lock-comment-face t)))))

;; This was an attempt to get more granularity, e.g. anything 
;; name[(]args[)] would hilight as a function in interpolations or user directives
;; Anything name would highlight as a variable name in interpolations or user directives
;; Syntax in interpolations (.(),+- etc.) would highlight as a differnet face.
;; But I couldn't figure out how to get "recursive" highlights (like a method as an
;; argument to a method...) to match a regex properly...  But I have some ideas...
;;
;; (defvar ftl-keywords
;;   (let 
;;       (
;;        (directive (concat "\\([<][/]?#\\(assign\\|if\\|elseif\\|else\\|foreach\\|"
;;                           "list\\|break\\|import\\|noparse\\|compress\\|"
;;                           "escape\\|noescape\\|global\\|local\\|setting\\|"
;;                           "switch\\|case\\|default\\|break\\|"
;; 			  "nested\\|return\\|flush\\|stop\\|macro\\ftl\\|"
;;                           "t\\|lt\\|rt"
;;                           ")[^>]*[>]\\)"))
;;        (user-directive "\\([<][/]?@[a-zA-Z][a-zA-Z0-9_]*[^>]*[>]\\)")

;;        (interpolation-all     "\\(\\$[{][^}]+[}]\\)")
;;        (interpolation-names   "\\$[{]\\(?\\([:alpha:][_[:alnum:]]*\\)[(),. ]*\\)[}]")
;;        (interpolation-methods "\\$[{]\\(?\\([:alpha:][_[:alnum:]]*\\)[(][^(}]\\)*[}]")
;;        (interpolation-syntax  "\\$[{]\\(?[^(),.\"']*\\([.,()\"']\\)\\)*[}]")
;;        (string  "\\(\"[^\"]*\"\\)")
;;        (comment "\\([<]#--[^>]*--[>]\\)"))


;;     (list
;;      (list interpolation-all '(1 font-lock-type-face t))
;;      (list interpolation-names '(1 font-lock-variable-name-face t))
;;      (list interpolation-methods '(1 font-lock-function-face t))
;;      (list interpolation-syntax '(1 font-lock-warning-face t))
;;      (list user-directive '(0 font-lock-function-name-face t))
;;      (list directive '(0 font-lock-keyword-face t))
;;      (list string    '(1 font-lock-string-face t))
;;      (list comment   '(0 font-lock-comment-face t)))))

(provide 'ftl)

