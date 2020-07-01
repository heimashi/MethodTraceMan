package cn.cxzheng.asmtraceman;

public class Test {

    void test11() {

    }

    private void test12() {

    }

    protected void test13() {

    }

    public void test14() {

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
