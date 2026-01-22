package com.jokeep.framework.config;

import java.util.ArrayList;
import java.util.List;

public class MultipleDataSource {

    private DataSourceBean master;

    private List<DataSourceBean> slaves;

    public DataSourceBean getMaster() {
        return master;
    }

    public void setMaster(DataSourceBean master) {
        this.master = master;
    }

    public List<DataSourceBean> getSlaves() {
        if (slaves == null)
            slaves = new ArrayList<>();
        return slaves;
    }
}
