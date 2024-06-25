package com.meteor.wechatbc.impl.event.listener;

import com.meteor.wechatbc.command.sender.ContactSender;
import com.meteor.wechatbc.entitiy.contact.Contact;
import com.meteor.wechatbc.entitiy.message.Message;
import com.meteor.wechatbc.event.EventHandler;
import com.meteor.wechatbc.impl.HttpAPI;
import com.meteor.wechatbc.impl.WeChatClient;
import com.meteor.wechatbc.impl.command.CommandManager;
import com.meteor.wechatbc.impl.event.Listener;
import com.meteor.wechatbc.impl.event.sub.MessageEvent;
import com.meteor.wechatbc.impl.event.sub.ReceiveMessageEvent;
import com.meteor.wechatbc.impl.model.message.ImageMessage;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.core.util.FileUtils;

import java.io.File;
import java.util.Optional;

/**
 * 监听微信用户执行指令
 */
@AllArgsConstructor
public class ContactCommandListener implements Listener {

    private WeChatClient weChatClient;

    @EventHandler
    public void onReceiveMessage(MessageEvent messageEvent){
        Message message = messageEvent.getMessage();
        if (messageEvent.getMessage() instanceof ImageMessage){
            ImageMessage receiveMessageEvent = (ImageMessage) messageEvent.getMessage() ;
            System.out.println(receiveMessageEvent.getContent());

        }
        if (message.getContent().equals("ding")) {
            Contact contact = messageEvent.getEventManager().getWeChatClient().getContactManager().getContactGroupCache().get(message.getFromUserName());
            final Contact.ContactMember groupMemberUser = contact.findGroupMemberUser(message.getSenderUserName());
//            contact.sendMessage("@" + groupMemberUser.getNickName() + " 你小子违规了 我要踢掉你");

            HttpAPI httpAPI = weChatClient.getWeChatCore().getHttpAPI();

//            httpAPI.delGroupMemberUser("@@cc7934f165fdc633f0ff594a3921fab2c3ad4650ab5acd813b100c4bf60783f0", "@8b24664c790f2e382dd0d016a2715b19cdc63d864e429182658dde8504dd1e4c");
//            httpAPI.addGroupMemberUser(contact.getUserName(), groupMemberUser.getUserName());
            httpAPI.delGroupMemberUser(contact.getUserName(), groupMemberUser.getUserName());
            httpAPI.modifyGroupName(contact.getUserName(), message.getContent());


        }

    }


    @EventHandler
    public void onCommand(MessageEvent messageEvent){
        String content = messageEvent.getContent();
        Message message = messageEvent.getMessage();
        String fromUserName = message.getFromUserName();
        Contact contact = weChatClient.getContactManager().getContactCache().get(fromUserName);
        if(contact==null) return;
        if(content.startsWith("/")){
            CommandManager commandManager = weChatClient.getCommandManager();
            CommandManager.ExecutorCommand executorCommand = commandManager.getExecutorCommand(content.replace("/",""));
            Optional.ofNullable(commandManager.getWeChatCommandMap().get(executorCommand.getMainCommand())).ifPresent(weChatCommand -> {
                weChatCommand.getCommandExecutor().onCommand(new ContactSender(weChatClient.getContactManager().getContactCache().get(message.getSenderUserName())
                        ,fromUserName
                        ),executorCommand.formatArgs());
            });
        }

    }

}
