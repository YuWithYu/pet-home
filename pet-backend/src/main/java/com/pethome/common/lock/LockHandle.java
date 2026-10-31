package com.pethome.common.lock;

/**
 * 分布式锁句柄：必须显式释放（建议 try-with-resources 或 finally）。
 */
public interface LockHandle extends AutoCloseable {

    /** 锁 key */
    String key();

    /** 本次持有的唯一标识（UUID），用于校验锁归属 */
    String owner();

    /** 锁是否仍然有效（未被他人抢占、未过期） */
    boolean isValid();

    /** 主动续期，成功返回 true */
    boolean renew();

    /** 释放锁：仅当锁仍属于自己时才会删除 */
    @Override
    void close();
}
