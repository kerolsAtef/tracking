package com.kerols2020.tracking;

public class Request
{
   private String name;
  public Request(){};

    public Request(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
