package com.meteor.wechatbc.impl.contact;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.meteor.wechatbc.entitiy.contact.GetBatchContact;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.impl.WeChatClient;
import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信联系人
 */
public class ContactManager {


    private class ContactCacheLoader implements CacheLoader<String,Contact>{
        @Override
        public @Nullable Contact load(@NonNull String key) throws Exception {
            return getContact(key);
        }
    }


    private class GroupContactCacheLoader implements CacheLoader<String,Contact>{
        @Override
        public @Nullable Contact load(@NonNull String key) throws Exception {
            return getGroupContact(key);
        }
    }

    private final WeChatClient weChatClient;

    @Getter private LoadingCache<String, Contact> contactCache
            = Caffeine.newBuilder().maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new ContactCacheLoader());

    @Getter private LoadingCache<String, Contact> contactGroupCache
            = Caffeine.newBuilder().maximumSize(1000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build(new GroupContactCacheLoader());

    @Getter private Map<String,Contact> contactGroupMap;


    private Map<RetrievalType,RetrievalStrategy> retrievalTypeRetrievalStrategyMap;

    @Getter private Map<String,Contact> contactMap;

    public ContactManager(WeChatClient weChatClient){
        this.weChatClient = weChatClient;
        this.contactMap = ref();
        this.contactGroupMap = new HashMap<>();

        this.retrievalTypeRetrievalStrategyMap = new HashMap<>();

        retrievalTypeRetrievalStrategyMap.put(RetrievalType.NICK_NAME,new NickNameStrategy(this.contactMap));
        retrievalTypeRetrievalStrategyMap.put(RetrievalType.USER_NAME,new UserNameStrategy());
        retrievalTypeRetrievalStrategyMap.put(RetrievalType.REMARK_NAME,new RemarkStrategy(this.contactMap));

        this.weChatClient.getLogger().info("联系人列表数量: "+contactMap.size());
    }

    /**
     * 根据搜索策略寻找用户
     * @param retrievalType
     * @param targetKey
     * @return
     */
    public Contact getContact(RetrievalType retrievalType,String targetKey){
        return retrievalTypeRetrievalStrategyMap.get(retrievalType).getContact(contactMap,targetKey);
    }

    public Contact getContactByNickName(String nickName){
        return retrievalTypeRetrievalStrategyMap.get(RetrievalType.NICK_NAME).getContact(contactMap,nickName);
    }

    public Contact getContactByRemark(String remark){
        return retrievalTypeRetrievalStrategyMap.get(RetrievalType.REMARK_NAME).getContact(contactMap,remark);
    }

    public Contact getContact(String userName){

        Contact user = weChatClient.getWeChatCore().getSession().getWxInitInfo().getUser();
        if(user.getUserName().equalsIgnoreCase(userName)){
            return user;
        }

        if(contactMap.containsKey(userName)) return contactMap.get(userName);

        else {
            this.contactMap = ref();
            return contactMap.get(userName);
        }
    }

    public Contact getGroupContact(String roomId){

        if(contactGroupMap.containsKey(roomId)) return contactGroupMap.get(roomId);

        else {
            this.contactGroupMap = refGroup(roomId);
            return contactGroupMap.get(roomId);
        }
    }

    /**
     * 刷新联系人列表
     */
    public Map<String,Contact> ref(){
        HttpAPI httpAPI = weChatClient.getWeChatCore().getHttpAPI();
        JSONObject response = httpAPI.getContact();
        JSONArray memberList = response.getJSONArray("MemberList");
        Map<String,Contact> map = new HashMap<>();
        for (int i = 0; i < memberList.size(); i++) {
            JSONObject jsonObject = memberList.getJSONObject(i);
            Contact contact = JSON.toJavaObject(jsonObject, Contact.class);
            contact.setWeChatClient(this.weChatClient);
            map.put(contact.getUserName(),contact);
        }
        return map;
    }

    /**
     * 刷新联系人列表
     */
    public Map<String,Contact> refGroup(String roomId){
        HttpAPI httpAPI = weChatClient.getWeChatCore().getHttpAPI();
        List<GetBatchContact> queryContactList = new ArrayList<>();
        GetBatchContact getBatchContact = new GetBatchContact();
        getBatchContact.setUserName(roomId);
        getBatchContact.setEncryChatRoomId("");
        queryContactList.add(getBatchContact);
        JSONObject response = httpAPI.batchGetContactDetail(queryContactList);
        JSONArray memberList = response.getJSONArray("ContactList");
        Map<String,Contact> map = new HashMap<>();
        for (int i = 0; i < memberList.size(); i++) {
            JSONObject jsonObject = memberList.getJSONObject(i);
            Contact contact = JSON.toJavaObject(jsonObject, Contact.class);
            contact.setWeChatClient(this.weChatClient);
            map.put(contact.getUserName(),contact);
        }
        return map;
    }

}
