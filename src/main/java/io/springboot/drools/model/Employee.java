package io.springboot.drools.model;

import lombok.Getter;
import lombok.Setter;


/**
 * 员工
 */
@Getter
@Setter
public class Employee {

    private Long id;

    private String name; //名字
    private int age;     //年龄
    private String sex;  //性别
    private int year;    //工作年限
    private int salary;  //工资
    public Employee(){}
    public Employee(Long id, String name, int age, String sex, int year, int salary) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.year = year;
        this.salary = salary;
    }
}
