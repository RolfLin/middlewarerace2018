package com.alibaba.dubbo.performance.demo.agent.registry.model;

import java.io.Serializable;

public class RequestBody implements Serializable {
    private String method;
    private String parameterTypesString;
    private String parameter;
    private String interfaceName;

    public RequestBody(String interfaceName, String method, String parameterTypesString, String parameter) {
        this.method = method;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
        this.interfaceName = interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setParameterTypesString(String parameterTypesString) {
        this.parameterTypesString = parameterTypesString;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getMethod() {
        return method;
    }

    public String getParameterTypesString() {
        return parameterTypesString;
    }

    public String getParameter() {
        return parameter;
    }

    public String getInterfaceName() {
        return interfaceName;
    }
}
