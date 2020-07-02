

package mondrian.spi.impl;

import javax.naming.NamingException;
import javax.sql.DataSource;


import mondrian.spi.DataSourceResolver;
import mondrian.util.StaticThreadLocal;

/**
 * Implementation of {@link DataSourceResolver} that looks up
 * a data source
 *
 * @author
 */
public class DenodoDataSourceResolver implements DataSourceResolver {
    /**
     * Public constructor, required for plugin instantiation.
     */
    public DenodoDataSourceResolver() {
    }

    public DataSource lookup(String dataSourceName) throws NamingException {
        ThreadLocal<String> threadLocalValue = new ThreadLocal<>();
       return  (DataSource)StaticThreadLocal.getThreadLocal().get();
    }
}

// End DenodoDataSourceResolver.java
