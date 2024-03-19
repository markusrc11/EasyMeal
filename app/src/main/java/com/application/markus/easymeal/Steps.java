package com.application.markus.easymeal;

/**
 * Created by markus on 17/05/2017.
 */

public class Steps {

    private String step_num, desc, img;

    public Steps(String step_num, String desc, String img) {
        this.step_num = step_num;
        this.desc = desc;
        this.img = img;
    }

    public String getStep_num() {
        return step_num;
    }

    public void setStep_num(String step_num) {
        this.step_num = step_num;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
