package com.spiritgame;

public interface SpiritClient {
    public abstract void doChat(int chatID, int[] chatpos, String chat);

    public abstract void updateEntity(Long ID, int[] attr);

    public abstract void removeEntity(Long ID);
}
