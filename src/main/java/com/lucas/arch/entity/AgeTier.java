package com.lucas.arch.entity;

public enum AgeTier {
    BABY(0.2f), 
    CHILD(0.45f), 
    JUVENILE(0.75f), 
    ADULT(1.0f);

    private final float scaleMultiplier;

    AgeTier(float scaleMultiplier) {
        this.scaleMultiplier = scaleMultiplier;
    }

    public float getScaleMultiplier() {
        return this.scaleMultiplier;
    }
}