# SnowFlakeResearch

package com.ule.retailordertest.order.controller;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * twitter的snowflake算法 -- java实现
 * 
 * @author beyond
 * @date 2016/11/26
 */
public class SnowFlake2 {

    /**
     * 起始的时间戳
     */
    private final static long START_STMP = 1514736000000L; //2018/1/1

    /**
     * 每一部分占用的位数
     */
    private final static long TIME_BIT = 41;
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long USER_BIT = 0;//用户ID后3位占用的位数
    //private final static long USER_BIT = 10;//用户ID后3位

    /**
     * 每一部分的最大值
     */
    private final static long MAX_USER_NUM = -1L ^ (-1L << USER_BIT);
    private final static long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long USER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTMP_LEFT = USER_LEFT + USER_BIT;

    private long userId;  //用户
    private long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStmp = -1L;//上一次时间戳

    private static volatile SnowFlake2 instance = null;
    
    public static SnowFlake2 GetInstance(long machineId) {
    	if(instance == null) {
        	synchronized(SnowFlake2.class) {
            	if(instance == null) {
            		instance = new SnowFlake2(machineId);
            	}
        	}
    	}
    	return instance;
    }
    
    public SnowFlake2(long machineId) {
       this(0,machineId);
    }
    
    private SnowFlake2(long userId, long machineId) {
        if (userId > MAX_USER_NUM || userId < 0) {
            throw new IllegalArgumentException("userId can't be greater than MAX_USER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.userId = userId;
        this.machineId = machineId;
    }

    /**
     * 产生下一个ID
     *
     * @return
     */
    public synchronized long nextId() {
        long currStmp = getNewstmp();
        if (currStmp < lastStmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        }

        if (currStmp == lastStmp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStmp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStmp = currStmp;
        
        return (currStmp - START_STMP) << TIMESTMP_LEFT //时间戳部分
                | userId << USER_LEFT       //用户部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getNewstmp();
        while (mill <= lastStmp) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();

    }
    
    private void Decode(long id) {
    	String bStr = Long.toBinaryString(id);
    	String bSeq = bStr.substring(bStr.length() - (int)SEQUENCE_BIT );
    	String bWork = bStr.substring(bStr.length() - (int)SEQUENCE_BIT - (int)MACHINE_BIT, bStr.length() - (int)SEQUENCE_BIT );
    	String bUser = bStr.substring(bStr.length() - (int)SEQUENCE_BIT - (int)MACHINE_BIT - (int)USER_BIT, bStr.length() - (int)SEQUENCE_BIT - (int)MACHINE_BIT );
    	String bTime = bStr.substring(0, bStr.length() - (int)SEQUENCE_BIT - (int)MACHINE_BIT - (int)USER_BIT);
    	
    	Long lTime = Long.parseLong(bTime, 2) + START_STMP;    	
    	Long lUser = Long.parseLong(bUser, 2);
    	Long lWork = Long.parseLong(bWork, 2);
    	Long lSeq = Long.parseLong(bSeq, 2);
    	
    	System.out.println(bStr);
    	System.out.println(String.format("%s %s %s %s", bTime,bUser,bWork,bSeq));
    	System.out.println(String.format("%d %d %d %d", lTime,lUser,lWork,lSeq));
    }
    

    public static void main(String[] args) {
    	
    	/*long x1 = 5214837243392002L;
    	System.out.println(Long.toBinaryString(x1));
    	
    	System.out.println(5 << 3 >> 3);
    	
    	System.out.println(UUID.randomUUID().toString());*/

        SnowFlake2 snowFlake = new SnowFlake2(0, 30);
        Long x2 = snowFlake.nextId();
        System.out.println(x2);
        snowFlake.Decode(x2);

    	long y2k = 946656000000L;
    	Calendar calendar = Calendar.getInstance();
    	calendar.set(2058, 8, 18, 28, 50, 30);
    	System.out.println(calendar.getTimeInMillis()-y2k);
        Timestamp time3 = new Timestamp(calendar.getTimeInMillis());
        System.out.println(time3);
        /*for (int i = 0; i < (1 << 12); i++) {
            System.out.println(snowFlake.nextId());
        }*/

    }
}
