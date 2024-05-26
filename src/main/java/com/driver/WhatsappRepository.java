
package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;


    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;

    }

    public Group createGroup(List<User> users) {

        Group group = new Group();

        if(users.size()==2){

            group.setName(users.get(1).getName());

        }
        else{
            customGroupCount++;
            group.setName("Group "+customGroupCount);

        }

        //Group group = new Group(customGroupCount+"",users.size());
        group.setNumberOfParticipants(users.size());
        groupUserMap.put(group,users);
        adminMap.put(group,users.get(0));
        //customGroupCount++;
        //admin problem
        return group;

    }


    public int createMessage(String content) {

        Message message = new Message(messageId,content,new Date());
        messageId++;
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{

        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");
        else if(!groupUserMap.get(group).contains(sender))
            throw  new Exception("You are not allowed to send message");

        if(!groupMessageMap.containsKey(group))
            groupMessageMap.put(group,new ArrayList<>());
        groupMessageMap.get(group).add(message);
        senderMap.put(message,sender);
        return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws  Exception {

        if(!groupUserMap.containsKey(group))
            throw new Exception("Group does not exist");

        else if(adminMap.get(group)!=approver)
            throw new Exception("Approver does not have rights");
        else if(!groupUserMap.get(group).contains(user))
            throw new Exception("User is not a participant");

        adminMap.put(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        int count = 0;
        boolean found = false;
        for(Group group : groupUserMap.keySet()){
            if(groupUserMap.get(group).contains(user)){
                if(adminMap.get(group)==user)
                    throw new Exception("Cannot remove admin");
                else {

                    int ind = groupUserMap.get(group).indexOf(user);
                    count += groupUserMap.get(group).size()-1-ind;
                    groupUserMap.get(group).remove(user);

                }
                found = true;

                if(groupMessageMap.containsKey(group)) {
                    for (Message message : groupMessageMap.get(group)) {
                        if (senderMap.get(message) == user) {
                            groupMessageMap.get(group).remove(message);
                            senderMap.remove(message);
                            count++;
                        }
                    }
                }

                userMobile.remove(user.getMobile());
                break;


//                if(groupUserMap.get(group).size()==0)
//                    groupUserMap.remove(group);

            }
        }
        if(!found)
            throw  new Exception("User not found");

        return count;

    }

    public String findMessage(Date start, Date end, int k) throws  Exception {

        TreeMap<Date,String> msg = new TreeMap<>();

        for(Message message : senderMap.keySet()){
            if(start.compareTo(message.getTimestamp())<0 && end.compareTo(message.getTimestamp())>0)
                msg.put(message.getTimestamp(),message.getContent());
        }

        int c = 1;
        Date date1= new Date();
        if(msg.size()<k)
            throw  new Exception("K is greater than the number of messages");

        for(Date date:msg.keySet()){
            if(c==k) {
                date1 = date;
                break;
            }
            c++;
        }

        return msg.get(date1);

    }

    public String createUser(String name, String mobile)throws  Exception {

        if(userMobile.contains(mobile))
            throw new Exception("User already exists");

        userMobile.add(mobile);
        return "SUCCESS";

    }
}
