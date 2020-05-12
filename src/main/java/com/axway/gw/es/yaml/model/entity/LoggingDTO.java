package com.axway.gw.es.yaml.model.entity;

import java.util.Objects;

public class LoggingDTO {

    public static final String FIELD_LOG_MASK_TYPE = "logMaskType";
    public static final String FIELD_LOG_MASK = "logMask";
    public static final String FIELD_LOG_FATAL = "logFatal";
    public static final String FIELD_LOG_FAILURE = "logFailure";
    public static final String FIELD_LOG_SUCCESS = "logSuccess";
    public static final String FIELD_ABORT_PROCESSING_ON_LOG_ERROR = "abortProcessingOnLogError";
    public static final String FIELD_CATEGORY = "category";

    private String logMaskType;
    private String logMask;
    private String logFatal;
    private String logFailure;
    private String logSuccess;
    private String abortProcessingOnLogError;
    private String category;

    public String getLogMaskType() {
        return logMaskType;
    }

    public String getLogMask() {
        return logMask;
    }

    public String getLogFatal() {
        return logFatal;
    }

    public String getLogFailure() {
        return logFailure;
    }

    public String getLogSuccess() {
        return logSuccess;
    }

    public String getAbortProcessingOnLogError() {
        return abortProcessingOnLogError;
    }

    public String getCategory() {
        return category;
    }

    public LoggingDTO setLogMaskType(String logMaskType) {
        this.logMaskType = logMaskType;
        return this;
    }

    public LoggingDTO setLogMask(String logMask) {
        this.logMask = logMask;
        return this;
    }

    public LoggingDTO setLogFatal(String logFatal) {
        this.logFatal = logFatal;
        return this;
    }

    public LoggingDTO setLogFailure(String logFailure) {
        this.logFailure = logFailure;
        return this;
    }

    public LoggingDTO setLogSuccess(String logSuccess) {
        this.logSuccess = logSuccess;
        return this;
    }

    public LoggingDTO setAbortProcessingOnLogError(String abortProcessingOnLogError) {
        this.abortProcessingOnLogError = abortProcessingOnLogError;
        return this;
    }

    public LoggingDTO setCategory(String category) {
        this.category = category;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoggingDTO that = (LoggingDTO) o;
        return Objects.equals(logMaskType, that.logMaskType) &&
                Objects.equals(logMask, that.logMask) &&
                Objects.equals(logFatal, that.logFatal) &&
                Objects.equals(logFailure, that.logFailure) &&
                Objects.equals(logSuccess, that.logSuccess) &&
                Objects.equals(abortProcessingOnLogError, that.abortProcessingOnLogError) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logMaskType, logMask, logFatal, logFailure, logSuccess, abortProcessingOnLogError, category);
    }

    @Override
    public String toString() {
        return "LoggingDTO{" +
                "logMaskType='" + logMaskType + '\'' +
                ", logMask='" + logMask + '\'' +
                ", logFatal='" + logFatal + '\'' +
                ", logFailure='" + logFailure + '\'' +
                ", logSuccess='" + logSuccess + '\'' +
                ", abortProcessingOnLogError='" + abortProcessingOnLogError + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
