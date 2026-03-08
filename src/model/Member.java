package model;

public class Member {

    private String memberID;
    private String name;
    private String email;

    public Member(String memberID, String name, String email) {
        this.memberID = memberID;
        this.name = name;
        this.email = email;
    }

    public String getMemberId() {
        return memberID;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return String.format("MemberID: %s | Name: %s | Email: %s", memberID, name, email);
    }
}