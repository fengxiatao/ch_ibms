package com.jokeep.business.DHSDK.structure;

import com.jokeep.business.DHSDK.NetSDKLib;

import static com.jokeep.business.DHSDK.NetSDKLib.NET_DEVICE_NAME_LEN;

/**
 * 电视墙预案名称 拆分自{@link NET_IN_MONITORWALL_GET_COLL_SCHD}
 *
 * @author ： 47040
 * @since ： Created in 2020/10/19 9:28
 */
public class NET_MONITORWALL_COLLECTION_NAME extends NetSDKLib.SdkStructure {
    /**
     * 电视墙预案名称
     */
    public byte[] collectionName = new byte[NET_DEVICE_NAME_LEN];

}
