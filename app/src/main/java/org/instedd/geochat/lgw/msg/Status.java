package org.instedd.geochat.lgw.msg;


import java.util.List;

public class Status {
    public List<String> confirmed;
    public List<String> failed;

    public Status(List<String> confirmed, List<String> failed) {
        this.confirmed = confirmed;
        this.failed = failed;
    }
}
