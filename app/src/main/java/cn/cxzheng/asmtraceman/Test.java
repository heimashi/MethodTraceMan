package cn.cxzheng.asmtraceman;

public class Test {

    public void test1() {

    }

    public void test2() {
        if (-1 > 0) {
            "xxxx".length();
        }
    }

    public static void test3() {
        if (-1 > 0) {
            "xxxx".length();
        }
    }

    static {
        test3();
    }

}
