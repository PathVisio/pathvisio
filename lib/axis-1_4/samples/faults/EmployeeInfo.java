package samples.faults;

import samples.faults.Employee;

import java.util.Collection;
import java.util.HashMap;

public class EmployeeInfo {
    static HashMap map = new HashMap();
    static {
        Employee emp = new Employee();
        emp.setEmployeeID("#001");
        emp.setEmployeeName("Bill Gates");
        map.put(emp.getEmployeeID(), emp);
    }

    public void addEmployee(Employee in) {
        map.put(in.getEmployeeID(), in);
    }

    public Employee getEmployee(java.lang.String id) throws NoSuchEmployeeFault {
        Employee emp = (Employee) map.get(id);
        if (emp == null) {
            NoSuchEmployeeFault fault = new NoSuchEmployeeFault();
            fault.setInfo("Could not find employee:" + id);
            throw fault;
        }
        return emp;
    }

    public Employee[] getEmployees() {
        Collection values = map.values();
        Employee[] emps = new Employee[values.size()];
        values.toArray(emps);
        return emps;
    }
}
