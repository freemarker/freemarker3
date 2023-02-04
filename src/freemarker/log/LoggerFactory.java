package freemarker.log;

/**
 * @version $Id: LoggerFactory.java,v 1.5 2003/01/12 23:40:17 revusky Exp $
 * @author Attila Szegedi
 */
interface LoggerFactory
{
    public Logger getLogger(String category);
}
