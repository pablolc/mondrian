
package mondrian.util;
import javax.sql.DataSource;
public class StaticThreadLocal {
    static private ThreadLocal threadLocal = new ThreadLocal<DataSource>();

    public static ThreadLocal getThreadLocal() {
        return threadLocal;
    }
}
