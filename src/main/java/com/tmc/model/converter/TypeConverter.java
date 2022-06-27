package com.tmc.model.converter;

public enum TypeConverter {
    CUSTOMER_TYPE, EMPLOYEE_TYPE, TIMESHEET_TYPE, COMPANY_TYPE;

    @SuppressWarnings("unchecked")
    public <T> T convert(String s) {
        switch (this) {
            case COMPANY_TYPE:
                CompanyConverter companyConverter = new CompanyConverter();
                return (T) companyConverter.unconvert(s);
            case EMPLOYEE_TYPE:
                EmployeeConverter employeeConverter = new EmployeeConverter();
                return (T) employeeConverter.unconvert(s);
            case TIMESHEET_TYPE:
                TimesheetConverter timesheetConverter = new TimesheetConverter();
                return (T) timesheetConverter.unconvert(s);
            case CUSTOMER_TYPE:
                CustomerConverter customerConverter = new CustomerConverter();
                return (T) customerConverter.unconvert(s);
            default:
                return null;
        }
    }
}
