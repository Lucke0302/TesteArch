package com.lucas.arch.entity;

public enum AgeTier {
    BABY(0.1f), 
    CHILD(0.35f), 
    JUVENILE(0.7f), 
    ADULT(1.0f);

    private final float scaleMultiplier;

    AgeTier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public float getScaleMultiplier() {
        return this.scaleMultiplier;
    }
}