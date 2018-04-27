
var app = new Vue({


    el:'#pcoded',
    data:{

        emailRecver:'',
        emailTitle:'',
        emailContent:'',
        lineUser:[],
        tempImg:[],
        friendList:[],
        accountCache:{
        },
        currentMessageList:[],
        willSendMessage:'',
        currentChatMessage:'',
        chatMessage:{

        },
        accountInfo:{
            username:'',
            auto_id:'',
            is_admin:'',
            img_path:'',
        },
        mainView:'main',
        pluginView:'',
        visibility:1,
        releaseInfo:{
            content:'',
            title:'',
            id:'',
            update:false,
        },
        messageList:[],
        commentMsg:'',
        messageInfo:{
            title:'',
            list:[]
        }
    },
    methods:{

        sendEmail:function(){
            var $this = this;
            $.post('../MessageAction/sendEmail.do',{
                title:this.emailTitle,
                content:this.emailContent,
                id:this.emailRecver
            },function (result) {
                if(result.code == 200){
                    alert("发送成功");
                    $this.emailTitle = '';
                    $this.emailContent = '';
                    $this.emailRecver = '';
                    $this.pluginView = 'list';
                }else{
                    alert('发送失败');
                }
            });
        },
        sendEmailToFriend:function(event,id){

            this.emailRecver = id;
            this.pluginView = 'email';
            event.stopPropagation();
        },
        isInLine:function(friend){
          var id = friend.friend;
          for(var i = 0;i<this.lineUser.length;i++){
              if(id == this.lineUser[i]){
                  return true;
              }
          }
          return false;
        },
        loadTempImg:function(){
            var $this = this;
          $.post('../MessageAction/loadTempFile.do',{dt:new Date().getTime()},function(result){

              if(result.code == 200){
                  $this.tempImg = result.list;
              }
          });
        },

        queryHistoryChat:function(id,friend){
            var $this = this;
            $.post('../MessageAction/queryHistoryChat.do',{
                sender:id,
                recver:friend,
                dt:new Date().getTime()
            },function(result){
                if(result.code == 200){
                    $this.currentMessageList = [];
                    for(var i = 0;i<result.list.length;i++){
                        $this.currentMessageList.push({
                            type:id == result.list[i].sender ? 'send':'recv',
                            content:result.list[i].message,
                            userId:id == result.list[i].sender ? result.list[i].sender : result.list[i].recver,

                        });
                    }
                }
            });
        },

        getFriendName:function(friend){
            try{
                if(friend == undefined){
                    return this.getAccountInfo(this.currentChatMessage.split("_")[1]).username;
                }
                return this.getAccountInfo(friend.friend).username;
            }catch (e){
                return '';
            }

        },
        isShowChat:function(node,index,m){

            if(index == 0 || this.accountInfo.is_admin == '1' ||node.visibility == '1' || m.accountId == this.accountInfo.auto_id){
                return true;
            }else if(node.visibility == '0'){

                if(node.accountId == this.accountInfo.auto_id){
                    return true;
                }
            }
            return false;
        },
        getFriendImg:function(friend){
            try{
                if(friend == undefined){
                    return this.getAccountInfo(this.currentChatMessage.split("_")[1]).img_path;
                }
                return this.getAccountInfo(friend.friend).img_path;
            }catch (e){
                return '';
            }

        },
        loadFriends:function(){
          this.pluginView = 'list';
        },
        delComment:function(node,index){
            var $this = this;
            this.messageInfo.list.splice(index,1);
            $.post('../MessageAction/delComment.do',{message:index == 0,id:node.id,dt:new Date().getTime()},function(result){
                if(result.code == 200){
                    if(index == 0){
                        $this.mainView = 'main';
                    }
                    alert('删除成功');
                    $this.queryMessage();
                }else{
                    alert('删除失败');
                }
            });
        },

        canDel:function(accountId){
            if(this.accountInfo.is_admin == '1'){
                return true;
            }
            return accountId == this.accountInfo.auto_id;
        },
        sendComment:function(){
            var $this = this;
            $.post('../MessageAction/sendComment.do',{
                dt:new Date().getTime(),
                id:this.messageInfo.list[0]['id'],
                visibility:this.visibility,
                message:this.commentMsg
            },function(result){

                if(result.code == 200){

                    $this.messageInfo.list.push({
                        content:$this.commentMsg,
                        icon:false,
                        accountId : $this.accountInfo.auto_id,
                        id:result.message,
                        comments:false,
                        visibility:$this.visibility,
                        date:$this.formatDate(),
                        images:'',
                        waitComment:'',
                        userImage:$this.accountInfo.img_path
                    });
                    $this.commentMsg = '';
                    alert('评论成功');
                }
            });
        },

        logout:function(){
          $.post('../MessageAction/logout.do',{dt:new Date().getTime()},function(result){
              window.location.href = 'login.html'
          });
        },

        updateMessageChat:function(node,index){

            var $this = this;
            if(!node.icon && index == 0){
                $.post('../MessageAction/addImgToTemp.do',{
                    dt:new Date().getTime(),
                    img:JSON.stringify(node.images)
                },function (result) {
                    if(result.code == 200){
                        $this.mainView = 'release';
                        $this.releaseInfo.title = $this.messageInfo.title;
                        $this.releaseInfo.content = node.content;
                        $this.releaseInfo.id = node.id;
                        $this.releaseInfo.update = true;
                        $this.tempImg = node.images;
                    }
                });

                return;
            }else if(node.icon){
                $.post('../MessageAction/updateMessageChat.do',{
                    dt:new Date().getTime(),content:node.content,
                    id:node.id
                },function(result){

                });
            }
            node.icon = !node.icon;

        },

        deleteCacheImg:function(img,index){
            this.tempImg.splice(index,1);
            $.post('../MessageAction/deleteImgCache.do',{dt:new Date().getTime(),img:img},function(result){
                if(code.result == 200){
                }
            });
        },

        toInfo:function(message){
            console.log(message);
            this.mainView = 'info';
            this.messageInfo.title = message.title;
            this.messageInfo.list = [];
            this.messageInfo.list.push({
                content:message.content,
                icon:false,
                visibility:message.visibility,
                accountId : message.account_id,
                id:message.auto_id,
                comments:false,
                date:this.formatDate(message.create_time),
                images:message.img_path.split(','),
                waitComment:'',
                userImage:message.user_img
            });
            for(var i = 0;i<message.comments.length;i++){
                var m = message.comments[i];
                this.messageInfo.list.push({
                    content:m.content,
                    visibility:m.visibility,
                    accountId : m.account_id,
                    icon:false,
                    id:m.auto_id,
                    comments:false,
                    date:this.formatDate(m.create_time),
                    images:'',
                    waitComment:'',
                    userImage:m.user_img
                });
            }
        },
        getImg:function(imgs){
            if(imgs.length == 0 || imgs == ''){
                return [];
            }
            return imgs.split(',');
        },
        release:function(){

            var param = {
                dt:new Date().getTime(),
                title:this.releaseInfo.title,
                content:this.releaseInfo.content,
                id:this.releaseInfo.id,
                update:this.releaseInfo.update
            };
            var $this = this;
            $.post('../MessageAction/release.do',param,function(result){

                $this.releaseInfo.update = false;
                $this.releaseInfo.title = '';
                $this.releaseInfo.content = '';
                $this.releaseInfo.id = '';
                if(result.code == 200){
                    alert('发布成功');
                    $this.tempImg = [];
                    $this.queryMessage();
                    $this.mainView = 'main';
                }else{
                    alert('发布失败');
                }
            });
        },
        sendMsgToUser:function(){
            if(this.willSendMessage == ''){
                alert('请出入要发送的消息');
                return;
            }
            var msg = {

                id:this.currentChatMessage.split("_")[1],
                userId:this.accountInfo.auto_id,
                type:'message',
                content:this.willSendMessage,

            };
            var $this = this;
            $.post('../MessageAction/addFriend.do',{
                dt:new Date().getTime(),
                self:this.accountInfo.auto_id,
                friend:this.currentChatMessage.split("_")[1],
            },function (result) {

                $this.queryFriend();

            });
            if(socket == null){
                createConnect(JSON.stringify(msg));
            }else{
                socket.send(JSON.stringify(msg));
            }


            this.currentMessageList.push({
                type:'send',
                sender:this.accountInfo.auto_id,
                recver:this.currentChatMessage.split("_")[1],
                content:this.willSendMessage
            });
            this.willSendMessage = '';
        },
        formatDate:function(time){
            var date = new Date();
            if(time){
                date = new Date(time);
            }

            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var day = date.getDate();
            var hours = date.getHours();
            var minutes = date.getMinutes();
            var seconds = date.getSeconds();

            return (year + '-'
            + (month < 10 ? '0'+month : month) + '-'
            + (day < 10 ? '0'+day : day )  + ' '
            + (hours < 10 ? '0'+hours : hours) +":"
            + (minutes < 10 ? '0'+minutes : minutes) +':'
            + (seconds < 10 ? '0'+seconds : seconds));
        },
        sendMessageToUser:function(id){
            this.pluginView = 'info';
            this.currentChatMessage = this.accountInfo.auto_id + '_' +id;
            this.queryHistoryChat(this.accountInfo.auto_id,id);

        },
        getAccountInfo:function(id){
            if(id == undefined){
                return null;
            }
            if(this.accountCache[id]){
                return this.accountCache[id];
            }else{
                var account = null;
                $.ajax({
                    url:'../MessageAction/queryAccountById.do',
                    dataType:'json',
                    type:'post',
                    async:false,
                    data:{
                        dt:new Date().getTime(),
                        id:id
                    },
                    success:function (result) {
                        if(result.code == 200){

                            account = result.account;
                        }
                    }
                });

                if(account!=null){

                    this.accountCache[id] = account;
                }
                return account;
            }
        },
        changeMessageToUser:function(friend){
          this.pluginView = 'info';
          this.currentChatMessage = this.accountInfo.auto_id+'_'+friend.friend;
          if(this.chatMessage[this.currentChatMessage] == undefined){
              this.chatMessage[this.currentChatMessage] = [];
          }
          this.currentMessageList = this.chatMessage[this.currentChatMessage];
          this.queryHistoryChat(this.accountInfo.auto_id,friend.friend);
        },
        queryMessage:function(){
            var $this = this;
            $.post('../MessageAction/queryList.do',{
                dt:new Date().getTime()
            },function(result) {
                    if (result.code == 200) {
                        $this.messageList = result.list;

                    }
                }
            );
        },
        queryAccountInfo:function(){
            var $this = this;
            $.post('../MessageAction/queryAccountInfo.do',{dt:new Date().getTime()},function(result){

                if(result.code == 200){
                    $this.accountInfo = result.info;
                    createConnect();
                }
            });
        },
        queryFriend:function(){
            var $this = this;
            $.post('../MessageAction/queryFriends.do',{dt:new Date().getTime()},function(result){

                if(result.code == 200){
                    $this.friendList = result.list;
                }
            });
        }


    },
    mounted:function(){
        this.queryAccountInfo();
        this.queryMessage();
        this.queryFriend();

    }
});

var socket = null;
function createConnect(ms){
    socket = new WebSocket('ws://'+document.domain+':8888/message');
    socket.onopen = function(){
        socket.send(JSON.stringify({
            type:'init',
            id:app.accountInfo.auto_id
        }));
        if(ms){
            socket.send(ms);
        }
    }

    socket.onmessage = function(event){
        var data = event.data;
        data = JSON.parse(data);
        if(data.type == 'message'){
            app.pluginView = 'info';


            var id = data.userId;
            var hasFriend = false;
            for(var i = 0;i<app.friendList.length;i++){
                if(app.friendList[i].friend == id){

                    hasFriend = true;
                    break;
                }
            }
            if(!hasFriend){
                $.post('../MessageAction/addFriend.do',{
                    dt:new Date().getTime(),
                    self:app.accountInfo.auto_id,
                    friend:id,
                },function (result) {

                });
            }
            app.currentChatMessage = app.accountInfo.auto_id + '_' + id;
            app.queryHistoryChat(app.accountInfo.auto_id,id);


        }else if(data.type == 'lineUser'){
            app.lineUser = data.lineUser;
        }
    }
}