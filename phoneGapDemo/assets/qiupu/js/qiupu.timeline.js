
var gtabsel = '';
var gpageidx = 0;//当前页码
var gneedfresh ='';
var objectId = "";
var picposthref = "";
var resLastPage = new Array(
		'You got last Page',
		'最后一页了',
		'você tem a última página',
		'あなたは最後のページを持って');
var nulltext = new Array(
		'Can not be empty!',
		'不能为空',
		'Não pode ser vazio',
		'空にすることはできません'
	);
var addfriendtext = new Array(
  		'Add friend ',
  		'加为好友',
  		'adicionar amigo ',
  		'友人を追加する '
  		);
var yourfriendtext = new Array(
  		'Your friend ',
  		'互为好友',
  		'o seu amigo',
  		'あなたの友人 '
  		);
var nexpagetext = new Array(
  		'Next Page ',
  		'下一页',
  		'próxima Página',
  		'次のページ'
  		);
var prepagetext = new Array(
  		'Previous page',
  		'上一页',
  		'página anterior',
  		'前のページ'
  		);

  		
var resMsgNotifytext = new Array (
		['Commented on the dynamic','Commented on your share of the dynamic','Shared his information to you','Application to share','Shared his link to you','you have new fans','Updated Contact','Updated gender','Updated the personal icon','Updated Company Name','Update the date of birth','Updated address','Updated profile','Updated displayname','Update the status'],
		['评论了动态','评论了您分享的动态','给您分享了他的信息','给您分享了应用','给您分享了他的链接','您有新的粉丝','更新了联系方式','更新了性别','更新了头像','更新了公司名称','更新了出生日期','更新了地址','更新了资料','更新了显示名称','更新了状态'],
		['Comentou sobre a dinâmica','Comentado em sua parte da dinâmica','Partilhou a sua informação para você','Aplicativo para compartilhar','Compartilhou seu link para você','Você tem um novo fã','Contato Atualizado','Atualizado sexo','A atualização da imagem','Nome da empresa Atualizado','Atualizar a data de nascimento','Endereço atualizado','Atualizar as informações','Atualize o nome de exibição','Atualizar o status de'],
		['ダイナミックにコメント','ダイナミックのシェアについてコメント','あなたに彼の情報を共有','共有への応用','あなたへの彼のリンクを共有','あなたは、新しいファンを持っています','更新お問い合わせ','更新された性別','画像を更新しました','更新された会社名','誕生日を更新','更新されたアドレス','情報を更新する','表示名を更新する','ステータスを更新']
	);
	 
var todaytext = new Array('Today','今天','Hoje','今日は ');
var deletext = new Array('Delete', '删除', 'excluir', '削除する');
var comtext = new Array('Comments', '评论', 'comentários', 'コメント');
var addcomtext = new Array('add comments', '评论', 'adicionar comentários', 'コメントを追加');
var repostext = new Array('Forward', '转发', 'encaminhamento', '転送');


function unix2human(value) {

	var unixTimeValue = new Date(value);
	beijingTimeValue = unixTimeValue.toLocaleString();
	//alert(unixTimeValue.getMonth());
	//Mon 20 Feb 2012 02:49:17 PM LONT
	if(unixTimeValue.getMinutes()<10){
		var Minute='0'+unixTimeValue.getMinutes();
	}
	else{
		var Minute=unixTimeValue.getMinutes();
	}
	pbtime = unixTimeValue.getFullYear() + "." + (unixTimeValue.getMonth() + 1) + "." + unixTimeValue.getDate() + "  " + unixTimeValue.getHours() + ":" + Minute;
	var today=new Date();
	if (today.getFullYear()==unixTimeValue.getFullYear()&&today.getMonth()==unixTimeValue.getMonth()&&today.getDate()==unixTimeValue.getDate()){
		pbtime="<span class='todaytxt'>"+todaytext[locale]+"</span>"+unixTimeValue.getHours()+ ":" + Minute;
	}
	
	return pbtime;
}

function callerror(ret)
{
	if (ret["error_msg"] == undefined) 
	{	//alert(JSON.stringify(ret));
		
		refreshed(gtabsel,gpageidx);
		//$.cookie('user_id', '');
		//$.cookie('ticket', '');
		//$.cookie('display_name', '');
		//$.cookie('login_name', '');
		//window.location = "login.html";
	}
}

/*
var i = 0;
function more(af) {
	i = i + 1;
	//alert("more" + i);
	af;
	return i;

}

function less(af) {
	i = i - 1;
	//alert("less" + i);
	af;
	return i;

}*/
var displayname = "";
function getdisplayname(o) {

	
	if(o.display_name != undefined) {
		displayname = o.display_name;
	} else {
		if(o.from != undefined) {
			displayname = o.from.display_name;
		}
	}

	var n = "<a title=\"" + displayname + "\" id=\""+getpersionid(o)+"persionid\" onclick=\"persionindex(this.getAttribute('id'))\" >" + displayname + "</a>"
	return n;
}

function geturspic(o) {
	var mge = "";
	if(o.small_image_url != undefined)
		var mge = o.small_image_url;
	else {
		if(o.from.image_url == undefined) {
			mge = "style/images/1.gif";
		} else{
			mge = o.from.image_url;
		}
	}
	//var mm = "<img style='float:left' src=" + mge + " onload=resizeimg(this,50,50)>"
	return mge;
}


function getpersionid(o) {
	var gpersionid = '';
	if(o.user_id !=undefined){
		gpersionid = o.user_id;
	}
	else{
		if(o.from != undefined) {
			gpersionid = o.from.user_id;
			} 
	}
	//alert(getpersionid+":"+getdisplayname(o));
	return gpersionid;
}

var persionid='';
function persionindex(persionids){
	persionid=persionids.substring(0, persionids.indexOf('persionid'));
	//alert(persionid);	
	//var url='persionindex.html?persionid=' + persionid;
	window.location ='persionindex.html?persionid=' + persionid;
	//alert(persionid+"persionindex.html?persionid=");
	return persionid;	
}


function getmessge(o) {
	var message='';
	if(o.message != undefined){
		message = httpcheck(getmsgprocessed(o.message));
			
	}	
	return message;
}



function httpcheck(s){
		var v = s;
		var reg = /(http:\/\/|https:\/\/)((\w|\+|=|\?|\.|\/|&|-|\%|\#|\:|\,|\!)+)/g;
		v = v.replace(reg, "<a title='$1$2' href='$1$2' target=\"blank\">$1$2</a>").replace(/\n/g, "<br/>");
		return v;
}

var pid = "";
function getpostid(o) {
	pid = o.post_id_s;
	//alert(pid);
	return pid;
}

function getmessge_create_time(o) {
	if(o.created_time != undefined)
		var messgae_createtime = o.created_time;
	return messgae_createtime;
}

function getdevice(o){
	var deviceinfo=o.device;
	var clienttext = new Array(
		'From client',
		'来自客户端',
		'do cliente',
		'クライアントからの'
	);
	var reg1 = new  RegExp('Firefox');
	var Firefox =  reg1.exec(deviceinfo);
	reg1 = new  RegExp('MSIE');
	var IE =  reg1.exec(deviceinfo);
	reg1 = new  RegExp('Safari');
	var Safari =  reg1.exec(deviceinfo);
	reg1 = new  RegExp('iPad');
	var iPad =  reg1.exec(deviceinfo);	
	reg1 = new  RegExp('iPhone');
	var iPhone =  reg1.exec(deviceinfo);	
	reg1 = new  RegExp('Android');
	var Android =  reg1.exec(deviceinfo);		
	reg1 = new  RegExp('Chrome');
	var Chrome =  reg1.exec(deviceinfo);	
	var reg2 = new  RegExp('client');
	var client =  reg2.exec(deviceinfo);
	if(client != null){
		var i=deviceinfo.indexOf('client=')+7;
		while (deviceinfo[i]!==';')
		{
			i=i+1;
		}
		var j=deviceinfo.indexOf('model=')+6;
		while (deviceinfo[j]!==';')
		{
			j=j+1;
		}
		deviceinfo=deviceinfo.substring(deviceinfo.indexOf('client=')+7,i)+" , "+deviceinfo.substring(deviceinfo.indexOf('model=')+6,j);
	}
	else {
		var fromtext = new Array(
			'From',
			'来自',
			'vir de',
			'から来る'
		);
		if(Firefox != null){
			deviceinfo=fromtext[locale]+" Firefox ";
		}
		if(IE != null){
			deviceinfo=fromtext[locale]+" IE ";	
		}
		if(Safari != null){
			deviceinfo=fromtext[locale]+" Safari ";	
		}
		if(iPad != null){
			deviceinfo=fromtext[locale]+" iPad";	
		}
		if(iPhone != null){
			deviceinfo=fromtext[locale]+" iPhone";
		}
		if(Android != null){
			deviceinfo=fromtext[locale]+" Android";
		}
		if(Chrome != null){
			deviceinfo=fromtext[locale]+" Chrome ";
		}			
	}

	return deviceinfo;
}

function gettopeople(o) {//get to people name

	var topeople = "";
	var to = "";
	if(o.to == undefined) {
		topeople = "";
		to = topeople;
	} else {
		for(var key = 0; key < o.to.length; key++) {
			if(o.to.length == 0) {
				topeople = "";
				to = topeople;
			} else {
				if(key < 4) {
					topeople += o.to[key].display_name + " ";
				} else {
					to = " to: " + topeople + "...";
					break;
				}
			}
			to = " to: " + topeople;
		}
	}
	return to;
}


function getcommentcount(o) {//get comments number
	var commentscount = 0;
	if(o.comments != undefined) {
		if(o.comments.count == undefined)
			commentscount = 0;
		else
			commentscount = o.comments.count;
	}
	return commentscount;
}

function showcomments(a) {//start-----------show all commets here
	//alert(a);
	a = a.substring(0, a.indexOf('comnt'));
	var x = '';
	var xx = '';
	x = a + "list_commment";
	xx = a + "post";	
	$("#"+x+"").toggle();
	$("#"+xx+"").css("display","none");

}

function getcomment(pid) {//start-----------get all commets here
	var cmm = "";
	//alert(pid);
	invokeApi("comment/for", prepareData({
		'target' : pid,
		'count':'-1'
	}), function(json) {
		cmm = comment(json);
		var a={};
		a=cmm.split("</ul>");
		var count=a.length-1;
		var list="<div class='pull-right' id="+pid+"funbox >"
			+"		<a class='deletcom' style='display:none' id="+pid+"delete onclick=postdelete(this.getAttribute('id'))>"+deletext[locale]+"</a>"
			+"		<span > </span>"
			+"		<a  class='comtext' id="+pid+"comnt onclick=\"showcomments(this.getAttribute('id'))\">"+comtext[locale]+"</a><span id='"+pid+"coun'>("+count+")</span>" 	
			+"		<span > </span>"
			+"		<a  id="+pid+"repost class='repostext' onclick=\"showsendrepost(this.getAttribute('id'))\">"+repostext[locale]+"</a>"
			+"	</div> <br>"
			+"<div class='list_commments well'  style='display:none;padding-bottom: 0px;padding-top: 9px;margin-top:5px;' id='"+pid+"list_commment'>"
			+"	<div><ul class='reply '  id='commentList' style='margin-left: 0px;'>"
			+		"<textarea  class='mytextares checkTextarea-area' id="+pid+"postmessgae onkeydown=\"limitChars(400)\" onchange=\"limitChars(400)\" onpropertychange=\"limitChars(400)\"></textarea>"
			+		"<button class='btn pull-right'  id="+pid+"create onclick=createcomment(this.getAttribute('id')) >"+addcomtext[locale]+"</button></ul></div>"
			+"	<div  id='comment_lists'>"+cmm+"</div></div>"
			+"<div class='post well' id="+pid+"post style='display:none;padding-bottom: 0px;padding-top: 9px;margin-top:5px;'"
			+"	<div><ul class='reply' style='margin-left: 0px;'>"
			+"		<textarea class='mytextares checkTextarea-area' id="+pid+"repostmessgae onkeydown=\"limitChars(400)\" onchange=\"limitChars(400)\" onpropertychange=\"limitChars(400)\" ></textarea>"
			+"		<button class='btn pull-right' id="+pid+"createpost onclick=createpost(this.getAttribute('id')) >"+repostext[locale]+"</button></ul></div></div>"
			+"<div  id="+pid+"repostsuc style='display:none;'><ul>success</ul></div>";	
		$("#"+pid+"public").html(list);		
		//showdeletecom(pid,persionid);
		//return cmm;
	}); 
	return cmm;//return null ,if not return null,there is undefined returned
}

function comment(json) {
	var cm = "";
	$.each(json, function(i, o) {
		cm += co(o);
	});
	return cm;

}

function co(o) {
	var cmt = "";	
	if(o.message == undefined) {
		cmt = "";
	} else {
		//cmt += o.commenter_name + ":" + o.message + "  <br>";
		cmt += "<hr><ul style='margin-left: 0px;' id=" + o.comment_id_s + ">" + "<a id=\""+o.commenter+"persionid\" onclick=\"persionindex(this.getAttribute('id'))\">"+o.commenter_name + "</a>:" + getmsgprocessed(o.message) + "</ul>";
	}
	return cmt;
	
}

function createcomment(t) {//start-----------create comment here
	//t.substring(0,t.indexOf('create'));
	if($.cookie('user_id') === '10471') {
		window.location = 'register.html';
		return;
	}		
	t = t.substring(0, t.indexOf('create'));
	var idinput = t + "postmessgae";
	var createcom = $("#" + idinput + "").val();
	var xx = t + "list_commment";
	var comcountid=t+"coun";
	var comcount= $("#" + comcountid + "").text();
	var comcount1=parseInt(comcount.substring(1,comcount.indexOf(')')))+1;
	$("#" + comcountid + "").html("("+comcount1+")");
	if(createcom.length>0){
		if(/^[\s'　']*$/.test(createcom)){
			alert(nulltext[locale]);
		}
		else{
			invokeApi("comment/create", prepareData({
				'target' : t,
				'message' : createcom
			}), function(json) {
			//return cmm;
			});		
			$("#" + xx + "").append("<hr><ul style='margin-left: 0px;'>" + "<a id=\""+ $.cookie('user_id')+"persionid\" onclick=\"persionindex(this.getAttribute('id'))\">"+ $.cookie('display_name') + "</a>:" + createcom + "</ul>");	
			$("#" + idinput + "").attr("value", "");		
		}
	}
	else{
		alert(nulltext[locale]);
	}
	//return cmm;//return null ,if not return null,there is undefined returned
}

function islike(o) {//start-----------like here
	var ilike = "";
	if(o.iliked) {
		return ilike = "赞(+1)";
	} else {
		return ilike = "赞";
	}//去赞
	//o.can_like //neng zan
}

function likeorunlike(c, t) {
	//t.substring(0,t.indexOf('like'))
	//alert (c);
	//alert(c.indexOf('('));
	if(c.indexOf('1') < 0) {
		alert("赞＋1");
		invokeApi("like/like", prepareData({
			'target' : t.substring(0, t.indexOf('like'))
		}), callerror);
		//invokeApi("post/liked", prepareData({'target':t}), call);

	} else {
		alert('去赞');
		invokeApi("like/unlike", prepareData({
			'target' : t.substring(0, t.indexOf('like'))
		}), callerror);

	}

	//$("." + lk + "").append("+");
}

function refreshed(gtabsel, pageidx) {	
	if(gtabsel == 'public') {
		publictimeline(pageidx);
	} else if(gtabsel == 'friend') {
		friendtimeline(pageidx);
	} else if(gtabsel == 'user') {
		usertimeline(pageidx);
	} else if(gtabsel == 'persionusers') {
		persionuserstimeline(pageidx);
	} else if(gtabsel == 'frishw') {
		friendshow(pageidx);
	} else if(gtabsel == 'folshw') {
		followershow(pageidx);
	} else if(gtabsel == 'pfolshw') {
		pfollowershow(pageidx);
	} else if(gtabsel == 'pfrishw') {
		//showFriendList();
		pfriendshow(pageidx);
	} else if(gtabsel == 'persioninfo') {
		//showFriendList();
		showpersion();
	} else if (gtabsel == 'searchuser') {
		searchusershow(gsearchname);
	} else if (gtabsel == 'circlepost') {
		circletimeline(pageidx);
	} else if (gtabsel == 'circlepeople') {
		showcirclepeoples(gcircleId);
	} else if (gtabsel == 'nfxshowread') {
		notificationread();
	};
	
	
}

function showsendrepost(postId) {//start-----------repost here
	var x = '';
	var xx = '';
	var p = '';
	p = postId.substring(0, postId.indexOf('repost'));
	x = p + "post";
	xx = p + "list_commment";

	$("#"+x+"").toggle();
	$("#"+xx+"").css("display","none");

}

function createpost(postId) {
    if($.cookie('user_id') === '10471') {
		window.location = 'register.html';
		return;
	}
	postId = postId.substring(0, postId.indexOf('createpost'));
	var idpostinput = postId + "repostmessgae";
	var createpost = $("#" + idpostinput + "").val();
	//alert(createpost);post
	var repostid = postId + "post";
	var repostsuc = postId + "repostsuc";
	invokeApi("post/repost", prepareData({
		'postId' : postId,
		'newmsg' : createpost
	}), callerror);
	var traget1 = document.getElementById(repostid);
	traget1.style.display = "none";
	var traget2 = document.getElementById(repostsuc);
	$("#" + idpostinput + "").attr("value", "");
	$("#"+repostsuc+"").fadeIn().delay('800').fadeOut();


}

function showdeletecom(id, p) {
	//alert(p+" "+$.cookie('user_id'));
	var tid = id + 'delete';
	//tid = id + "delete";
	//alert(tid+" "+p);
	if(p == $.cookie('user_id')) {
		document.getElementById(tid).style.display = '';
	}
}

function postdelete(postId) {
	postId = postId.substring(0, postId.indexOf('delete'));
	//alert(postId);
	var deleteinfo = new Array(
  		'Are you sure to delete this message?',
  		'确定删除此消息?',
  		'Tem certeza que deseja apagar esta mensagem?',
  		'このメッセージを削除してもよろしいですか？'
  	);
	if(confirm(deleteinfo[locale])) {//如果是true ，那么就把删除此消息
		invokeApi("post/delete", prepareData({
			'postIds' : postId
		}), callerror);
	} 

}

function showmoreattachment(object) {	
	var attachmentid=object.id.substring(0, object.id.indexOf('m'));
	var moinfoid=attachmentid+'info';
	$("#"+moinfoid+"").toggle();
	$("#"+object.id+"").toggle();
}

function hidemoreattachment(object) {	
	var attachmentid=object.id.substring(0, object.id.indexOf('l'));
	var moinfoid=attachmentid+'info';
	var moreid=attachmentid+'m';
	$("#"+moinfoid+"").toggle();
	$("#"+moreid+"").toggle();

}

function getrepost(o) {
	var repostmessage = "";
	var sharetext = new Array(
  		'Share message : ------',
  		'转发：------',
  		'Compartilhar a mensagem： ------',
  		'メッセージを共有する： ------'
  	);
  	var sharedeletetext = new Array(
  		'The original message has been deleted',
  		'原消息已经被删除',
  		'A mensagem original foi apagado',
  		'元のメッセージは削除されました'
  	);
	if(o.retweeted_stream != undefined) {
		if (o.retweeted_stream != ""){
		   repostmessage=
		   "<p style='color: silver;'>"+sharetext[locale]+"<br></p>"
		   +"<div class='well'>"
		   +"<img src=" +geturspic(o.retweeted_stream)+ " onload=resizeimg(this,20,20)>"
		   +getdisplayname(o.retweeted_stream)
		   +gettopeople(o.retweeted_stream)
		   +"<br>"
		   +getmessge(o.retweeted_stream)
		   +"<br>"		
		   +getshareinfo(o.retweeted_stream)
	       +"</div>";
		}
		else{
			repostmessage="<p style='color: silver;'>"+sharetext[locale]+"<br></p>"
		   +"<div class='well'>"
		   +sharedeletetext[locale]
		   +"</div>";
		}
	}
	return repostmessage;
}

function mouseon(thisob){	
	//thisob.style.color='red';requestaddfriend
	var tid='';
	if(thisob.id.indexOf('u')>0){
		var postid=thisob.id.substring(0,thisob.id.indexOf('u'));
		var persionid= thisob.id.substring(thisob.id.indexOf('u')+1,thisob.id.length);
		var isfriendid=persionid+"requestaddfriend";
		showdeletecom(postid,persionid);
		if(document.getElementById(isfriendid)!=null){
			document.getElementById(isfriendid).style.display = '';
		}		
	}else if(thisob.id.indexOf('follower')>0){
		var isfriendid=thisob.id.substring(0,thisob.id.indexOf('follower'))+"requestaddfriend";
		document.getElementById(isfriendid).style.display = '';
	}
}
function mouseout(thisob){
	var tid='';
	if(thisob.id.indexOf('u')>0){
		var tid=thisob.id.substring(0,thisob.id.indexOf('u'))+'delete';
		var persionid= thisob.id.substring(thisob.id.indexOf('u')+1,thisob.id.length);
		//var isfriendid=persionid+"requestaddfriend";
		//if(document.getElementById(isfriendid)!=null){
		//	document.getElementById(isfriendid).style.display = 'none';
		//}	
		document.getElementById(tid).style.display = 'none';	
	//}else if(thisob.id.indexOf('follower')>0){
	//	var isfriendid=thisob.id.substring(0,thisob.id.indexOf('follower'))+"requestaddfriend";
	//	document.getElementById(isfriendid).style.display = 'none';
	}	
}
		
function inserttimeline(json){	
	$.each(json,function(i,o) {
		isshow = true;				
		if((o.mentions != undefined))
		{
			if(o.mentions != ''){				
				var idgroup = o.mentions.split(",");
				tmpvl = false;
				var j=0;
				for(j=0;j<idgroup.length;j++)
				{										
					if(idgroup[j] == $.cookie('user_id'))
					{
						tmpvl = true;
						break;
					}
						
				}				
				if(o.source == $.cookie('user_id') ||(tmpvl == true)||(o.privince==false))
					isshow = true;				
				else
					isshow = false;
			}
		}				
		if(isshow == true)
		{
			var list2="<div class='row' id="+getpostid(o)+"u"+getpersionid(o)+" onmouseover=mouseon(this) onMouseOut=mouseout(this)>"  //style='border:1px solid #DDD' 
			+"<div class='hideOnPhone span1' style=\"width:48px\" >"
			+"<a  class='hideOnPhone'  >"
			+"<img src=" +geturspic(o) + " onload=resizeimg(this,48,48)>"
			+"</a>		"
			+"</div>"
			+"<div class='span5 myspan5' >"
			+"<div class='msgcnt' >"
			+"	<div class='msgbox'>"
			+"		<div class='userName' >"
			+				getdisplayname(o)+gettopeople(o)
			+		ishisfriend(o)
			+"		</div>"	
			+"	</div>"
			+		getmessge(o)			
			+"	<div class='mediaWrap' >"
			+	getrepost(o)
			+"		<div class='share'>	"
			+			getshareinfo(o)
			+"		</div>	"
			+"	</div>"
			+"</div>"
			+"	<div class='pull-left' style='color: silver;'> "
			+"		"+unix2human(getmessge_create_time(o))
			+"	"+getdevice(o)
			+"	</div>"
			+"<div id='"+getpostid(o)+"public' class='public'>"
			//+"<br>"
			+"</div>"
			+getcomment(pid)
			+"</div>"
			+"</div><hr>";
			$("#timelines").append(list2);			
			}		
		}
		); 		
}


function additem(json) {
	document.getElementById('timelines').innerHTML = "";
	inserttimeline(json);
}

 
var winWidth = 0;

var winHeight = 0;

function beforeload() {	

	if(window.innerWidth) winWidth = window.innerWidth;	
	else if((document.body) && (document.body.clientWidth))	winWidth = document.body.clientWidth;
	
	//获取窗口高度
	if(window.innerHeight) winHeight = window.innerHeight;	
	else if((document.body) && (document.body.clientHeight)) winHeight = document.body.clientHeight;
	
	//通过深入Document内部对body进行检测，获取窗口大小
	if(document.documentElement && document.documentElement.clientHeight && document.documentElement.clientWidth) {
		winHeight = document.documentElement.clientHeight;
		winWidth = document.documentElement.clientWidth;

	}
	document.getElementById('img').style.left=""+(winWidth/2)+"px";
	document.getElementById('img').style.top=""+(winHeight/2)+"px";
	document.getElementById('img').innerHTML = "<a><img style='BORDER:none' src='style/images/loading.gif'></a>";

}
function afterload() {
	document.getElementById('img').innerHTML = '';
}

//全局，其他js文件需要
var gnextshow = 0;//进入下一轮显示页面


var gfactpages = 3; //此轮应显示几页(实际页数)
var gpagenumshow = 3;//每轮显示页码数量
var gpnexttimes = 0;//第几轮
//itemcount:每页显示条目数


//draw the page
function pagelink(pages,startpage,pageindex,fun){
	var str='';
	document.getElementById('pageshow').innerHTML = "";
	//draw the page number(1,2,3)
	for(var j = 0;j< pages;j++)
		if(startpage+j == pageindex)
				str = str + "<li class='active'><a onclick = ''>"+(pageindex+1)+"</a></li>";
			else
				str = str + "<li><a onclick = '"+fun+"("+(startpage+j)+")'>"+(startpage+j+1)+"</a></li>";	
	
	
	//draw the prepage and nextpage,and calculate whether should fresh the page when click 
	
	if(pageindex>0)
		$("#pageshow").append("<li><a onclick='"+fun+"("+(pageindex-1)+")'><<</a></li>"+str+"</a></li>"+"<li><a onclick='"+fun+"("+(pageindex+1)+")'>>></a></li>");
	else
		$("#pageshow").append(str+"</a></li>"+"<li><a onclick='"+fun+"("+(pageindex+1)+")'>>></a></li>");
}

function showpage(pageindex,fun,gpnexttimes){
	
	
	//the start page
	var startpage = gpnexttimes*gpagenumshow;		
	
	pagelink(3,startpage,pageindex,fun);	
}
/*
function showpage(pageindex,url,fun,gpnexttimes,data,itemcount,needfreshpage){
	
	var str = "";
	//the start page
	var startpage = gpnexttimes*gpagenumshow;		
	
	if(needfreshpage == true)//need change the pages
	{
		invokeApi(url,data, function(json) {
		if(json=='')
		{
			//alert("no timeline");				
			return;
		}
		else
		{	
			document.getElementById('pageshow').innerHTML = "";
			//count the fact pages count
			if(json.length < itemcount)
				gfactpages = 1;
			else{
				if(json.length%itemcount == 0)
					gfactpages = parseInt(json.length/itemcount);
				else
					gfactpages = parseInt(json.length/itemcount)+1;
			}
			
			pagelink(gfactpages,startpage,pageindex,fun);
			
		}					
		});
		
	}	
	else
	{
		pagelink(gfactpages,startpage,pageindex,fun);
	}
}

//draw the page
function pagelink(pages,startpage,pageindex,fun){
	var str='';
	document.getElementById('pageshow').innerHTML = "";
	//draw the page number(1,2,3)
	for(var j = 0;j< pages;j++)
		if(startpage+j == pageindex)
				str = str + "<li class='active'><a onclick = ''>"+(pageindex+1)+"</a></li>";
			else
				str = str + "<li><a onclick = '"+fun+"("+(startpage+j)+",false)'>"+(startpage+j+1)+"</a></li>";	
	
	
	//draw the prepage and nextpage,and calculate whether should fresh the page when click 
	var prefresh = false;
	var nextfresh = false;
	if(parseInt(pageindex%gpagenumshow)==0)
		prefresh = true;
	if(parseInt(pageindex%gpagenumshow)==2)
		nextfresh = true;
	if(pageindex>0)
		$("#pageshow").append("<li><a onclick='"+fun+"("+(pageindex-1)+","+prefresh+")'><<</a></li>"+str+"</a></li>"+"<li><a onclick='"+fun+"("+(pageindex+1)+","+nextfresh+")'>>></a></li>");
	else
		$("#pageshow").append(str+"</a></li>"+"<li><a onclick='"+fun+"("+(pageindex+1)+","+nextfresh+")'>>></a></li>");
}*/

function hottimeline(pageidx){

	
	fun = 'hottimeline';	
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	/*
	url = 'post/hot';
	data = prepareData({
		'users' : $.cookie('user_id'),
		'page' : gpnexttimes,
		'count':gpagenumshow*20,		
		'cols':'post_id'
	});

	showpage(i,url,fun,gpnexttimes,data,20,needfreshpage);*/
	
	gpageidx = pageidx;
	gtabsel = 'hot';
	$('#hot').addClass('active');
	$('#friend').removeClass();
	$('#user').removeClass();	
	$('#public').removeClass();	
	beforeload();
	invokeApi("post/hot", prepareData({
		'users' : $.cookie('user_id'),
		'page' : pageidx
	}), function(json) {
			if(json==''){
				afterload();
				if(pageidx==0){
					document.getElementById('timelines').innerHTML = "";
				}else{
					alert(resLastPage[locale]);
					pageidx=pageidx-1;}
			}
			else{				
				document.getElementById('timelines').innerHTML = "";
				inserttimeline(json);
				afterload();		
				showpage(pageidx,fun,gpnexttimes);
				
			}
	});	

	
}
function friendtimeline(pageidx) {	
	$("#postcontent").addClass("hideOnPhone");
	$("#recadd").addClass("hideOnPhone");
	$("#content").removeClass("hideOnPhone");	
	$('#index-post').css({'display':'block'});
	fun = 'friendtimeline';
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	/*
	url = 'post/qiupufriendtimeline';
	data = prepareData({
		'users' : $.cookie('user_id'),
		'page' : gpnexttimes,
		'count':gpagenumshow*20,		
		'cols':'post_id'
	});

	showpage(i,url,fun,gpnexttimes,data,20,needfreshpage);	*/
	
	
	$('#maincontent').show();
	$('#usercontent').hide();	
	$('#nfxcontent').hide();
	
	gpageidx = pageidx;
	gtabsel = 'friend';
	$('#friend').addClass('active');
	$('#user').removeClass();	
	$('#public').removeClass();	
	$('#hot').removeClass();
	//for top menu on phone
	$('#friendtop').addClass('active');
	$('#index-notice').removeClass();	
	$('#publictop').removeClass();	
	
	beforeload();
	invokeApi("post/qiupufriendtimeline", prepareData({
		'users' : $.cookie('user_id'),
		'page' : pageidx
	}), function(json) {
			if(json==''){
				afterload();
				if(pageidx==0){
					document.getElementById('timelines').innerHTML = "";
				}else{
					alert(resLastPage[locale])
					pageidx=pageidx-1;}
			}
			else{				
				document.getElementById('timelines').innerHTML = "";
				inserttimeline(json);
				afterload();	
				
				//showpage(i,url,fun,gpnexttimes,data,20,needfreshpage);
				showpage(pageidx,fun,gpnexttimes);
			}
	});	
}

function publictimeline(pageidx) {
	$('#index-post').css({'display':'block'});		
	$("#postcontent").addClass("hideOnPhone");
	$("#recadd").addClass("hideOnPhone");
	$("#content").removeClass("hideOnPhone");
	fun = 'publictimeline';
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	
	/*url = 'post/qiupupublictimeline';
	data = prepareData({
		'users' : $.cookie('user_id'),
		'page' : gpnexttimes,
		'count':gpagenumshow*20,		
		'cols':'post_id'
	});

	showpage(i,url,fun,gpnexttimes,data,20,needfreshpage);*/		
	$('#nfxcontent').hide();	
	$('#maincontent').show();
	gpageidx = pageidx;
    gtabsel = 'public';
    $('#public').addClass('active');
	$('#user').removeClass();	
	$('#friend').removeClass();
	$('#hot').removeClass();
	//for top menu on phone
	$('#publictop').addClass('active');
	$('#index-notice').removeClass();	
	$('#friendtop').removeClass();
	
	beforeload();
	invokeApi("post/qiupupublictimeline", prepareData({
		'users' : $.cookie('user_id'),
		'page' : pageidx
	}), function(json) {
		//alert(JSON.stringify(json));
			if(json==''){
			//alert(json);
			afterload();
			if(pageidx==0){
				document.getElementById('timelines').innerHTML = "";
			}else{
				alert(resLastPage[locale])
				pageidx=pageidx-1;}
			}
			else{			
				document.getElementById('timelines').innerHTML = "";
				inserttimeline(json);
				afterload();
				showpage(pageidx,fun,gpnexttimes);
				/*
				$("#timelines").append("<ul class='pager'><li class='next'><a onclick = more(publictimeline(i+1))>下一页 &rarr;</a></li></ul>");
				if(i > 0) {
					$(".next").before("<li class='previous'><a onclick = less(publictimeline(i-1))>&larr; 上一页</a></li>");
				}*/
			}
	});
	//	prepareData({'users' : $.cookie('user_id'),'page' : i}) {'appid':'1','page' : i}

}



function circletimeline(pageidx) {		
	$("#postcontent").addClass("hideOnPhone");
	$("#recadd").addClass("hideOnPhone");
	$("#content").removeClass("hideOnPhone");
	gpageidx = pageidx;
	gtabsel = 'circlepost';
	
	$('#usercontent').hide();
	$('#nfxcontent').hide();	
	$('#maincontent').show();		
	$('#tabs').hide();	
	
	$('#tabcircles').show();	
	$('#circlepost').addClass('active');
	$('#circlepeople').removeClass();	
	
	document.getElementById('timelines').innerHTML = "";
	if(gcircleNum==0) return;
	
	//need preview the page count could show
	
	fun = 'circletimeline';
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	/*
	url = 'post/qiupufriendtimeline';
	data = prepareData({
		'users' : $.cookie('user_id'),
		'page' : gpnexttimes,
		'count':gpagenumshow*20,	
		'circleIds': gcircleId,	
		'cols':'post_id'
	});

	showpage(i,url,fun,gpnexttimes,data,20,needfreshpage);	*/
	
	beforeload();	
	invokeApi("post/qiupufriendtimeline", prepareData({
		'users' : $.cookie('user_id'),
		'page' : pageidx,
		'circleIds': gcircleId
	}), function(json) {
			if(json==''){
				afterload();
			    if(pageidx==0){
				    document.getElementById('timelines').innerHTML = "";
			    }else{
				    alert(resLastPage[locale])
				    pageidx=pageidx-1;
			    }
			}
			else{				
				document.getElementById('timelines').innerHTML = "";
				inserttimeline(json);
				afterload();
				showpage(pageidx,fun,gpnexttimes);
				/*
				$("#timelines").append("<ul class='pager'><li class='next'><a onclick = more(circletimeline(i+1))>下一页 &rarr;</a></li></ul>");
				if(i > 0) {
					$(".next").before("<li class='previous'><a onclick = less(circletimeline(i-1))>&larr; 上一页</a></li>");
				}*/
			}
	}
	);
	
}


/*		document.getElementById('timelines').innerHTML = "";
 inserttimeline(json);
 $("#timelines").append("<dt><button onclick = more(friendtimeline(page+1))> 下一页 </button></dt>");
 if(page > 0) {
 $("#timelines").append("<dt><button onclick = less(friendtimeline(page-1))> 上一页 </button></dt>");
 }
 */


function usertimeline(pageidx) {
	$('#index-post').css({'display':'block'});		
	$("#postcontent").addClass("hideOnPhone");
	$("#recadd").addClass("hideOnPhone");
	$("#content").removeClass("hideOnPhone");
	fun = 'usertimeline';
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	/*
	url = 'post/userstimeline';
	data = prepareData({
		'users' : $.cookie('user_id'),
		'page' : gpnexttimes,
		'count':gpagenumshow*20,			
		'cols':'post_id'
	});

	showpage(i,url,fun,gpnexttimes,data,20,needfreshpage);*/		
	$('#nfxcontent').hide();	
	$('#maincontent').show();
	gpageidx = pageidx;
	gtabsel = 'user';
	$('#user').addClass('active');
	$('#public').removeClass();	
	$('#friend').removeClass();
	$('#hot').removeClass();
	
	beforeload();
	invokeApi("post/userstimeline", prepareData({
		'users' : $.cookie('user_id'),
		'page' : pageidx
	}), function(json) {
			if(json==''){
				afterload();
			if(pageidx==0){
				document.getElementById('timelines').innerHTML = "";
			}else{
				alert(resLastPage[locale])
				pageidx=pageidx-1;}
			}
			else{
				document.getElementById('timelines').innerHTML = "";
				inserttimeline(json);
				afterload();
					
				showpage(pageidx,fun,gpnexttimes);
				/*
				$("#timelines").append("<ul class='pager'><li class='next'><a onclick = more(usertimeline(i+1))>下一页 &rarr;</a></li></ul>");
				if(i > 0) {
					$(".next").before("<li class='previous'><a onclick = less(usertimeline(i-1))>&larr; 上一页</a></li>");
				}*/				
			}
	});
}


/*用户查找结果*/
function searchusershow(username) 
{
	gsearchname=username;
	gtabsel = 'searchuser';
	$('#nfxcontent').hide();		
	$('#maincontent').hide();
	$('#usercontent').show();
	
	var searchlisttext = new Array(
		'Found users:',
		'查找到用户：',
		'Encontrar o usuário',
		'ユーザーを探す:'
	);
	
	var searchsigntext = new Array(
		'Signature:',
		'签名：',
		'assinatura:',
		'署名:'
	);	
			
	beforeload();
	invokeApi("account/search", 
	     prepareData({'username' : username}), 
	     function(json) {
		     	document.getElementById('userlines').innerHTML = "";
		     	var memcnt=0;
				$.each(json, function(i, o) {
					++memcnt;					
					if(o["status"]!= undefined&&o["status"]!=''){
						var status= searchsigntext[locale] +o["status"];						
					}
					else{
						status='';
					}
					$("#userlines").append("<div class='row' id="+o.user_id+"follower onmouseover=mouseon(this) onMouseOut=mouseout(this)>" 
						+"<div class='hideOnPhone span1' "
						+"<a class='hideOnPhone' >"
					    +"<img src=" + geturspic(o) +" onload=resizeimg(this,60,60)></a></div>"
					    +"<div class='span5' style='margin-left:15px;'>"
					    +getdisplayname(o)
						+ishisfriend(o)
						+ "<div id="+o.user_id+"status >"+status +"</div><br>"
						+getlastmessage(o.user_id)
					    +"</div>"
						+ "</div><hr>");
				});
				$("#usertitle").html(searchlisttext[locale] + '('+ memcnt +')');
				afterload();	
	});
}

function friendshow(pageidx) {
	gpageidx = pageidx;
	gtabsel = 'frishw';
	var friendslisttext = new Array(
		'Friends list',
		'好友列表',
		'lista de Amigos',
		'友達リスト'
	);

	$('#maincontent').hide();
	$('#nfxcontent').hide();	
	$('#usercontent').show();	
	beforeload();
	invokeApi("friend/show", prepareData({
		'user' : $.cookie('id'),
		'page' : pageidx
	}), function(json) {
			if(json==''){
				afterload();
			if(pageidx==0){
				document.getElementById('userlines').innerHTML = "";
			}else{
				alert(resLastPage[locale])
				pageidx=pageidx-1;}
			}
			else{
				document.getElementById('userlines').innerHTML = "";
		     	$("#usertitle").html(friendslisttext[locale]);
				$.each(json, function(i, o) {
					if(o["status"]!= undefined&&o["status"]!=''){
						var status=o["status"];
					}
					else{
						status='';
					}
					$("#userlines").append("<div class='row' id="+o.user_id+"follower onmouseover=mouseon(this) onMouseOut=mouseout(this)>" 
						+"<div class='hideOnPhone span1' "
						+"<a class='hideOnPhone' >"
					    +"<img src=" + geturspic(o) +" onload=resizeimg(this,60,60)></a></div>"
					    +"<div class='span5' style='margin-left: 15px;' >"
					    +getdisplayname(o)
					    //+ishisfriend(o)
						+ "<div id="+o.user_id+"status >"+status +"</div><br>"
						+getlastmessage(o.user_id)
					    +"</div>"
						+ "</div><hr>");
				});
				afterload();
				//showpage(pageidx,fun,gpnexttimes);
				$("#userlines").append("<ul class='pager'><li class='next'><a onclick = friendshow("+(pageidx+1)+")>"+nexpagetext[locale]+" &rarr;</a></li></ul>");
				if(pageidx > 0) {
					$(".next").before("<li class='previous'><a onclick = friendshow("+(pageidx-1)+")>&larr; "+prepagetext[locale]+"</a></li>");
				}
			}
	});
}


function pfriendshow(pageidx){
	friendshow(pageidx);
	gtabsel = 'pfrishw';
}

function followershow(pageidx) {
	gpageidx = pageidx;
	gtabsel = 'folshw';
	var followlisttext = new Array(
		'Follows list',
		'粉丝列表',
		'lista de seguidores',
		'フォロワーの一覧'
	);
	$('#maincontent').hide();
	$('#nfxcontent').hide();	
	$('#usercontent').show();
	
	beforeload();
	invokeApi("follower/show", prepareData({
		'user' : $.cookie('id'),
		'page' : pageidx
	}), function(json) {
			if(json==''){				
				if(pageidx==0){
					document.getElementById('userlines').innerHTML = "";					
				}else{
					alert(resLastPage[locale]);
					pageidx=pageidx-1;
				}
				afterload();
			}
			else{
				document.getElementById('userlines').innerHTML = "";
		     	$("#usertitle").html(followlisttext[locale]);
				$.each(json, function(i, o) {					
					if(o["status"]!= undefined&&o["status"]!=''){
						var status=o["status"];						
					}
					else{
						status='';
					}
					$("#userlines").append("<div class='row' id="+o.user_id+"follower onmouseover=mouseon(this) onMouseOut=mouseout(this)>" 
						+"<div class='hideOnPhone span1'"
						+"<a class='hideOnPhone' >"
					    +"<img src=" + geturspic(o) +" onload=resizeimg(this,60,60)></a></div>"
					    +"<div class='span5' style='margin-left: 15px;'>"
					    +getdisplayname(o)
						+ishisfriend(o)
						+ "<div id="+o.user_id+"status >"+status +"</div><br>"
						+getlastmessage(o.user_id)
					    +"</div>"
						+ "</div><hr>");
				});
				afterload();
				$("#userlines").append("<ul class='pager'><li class='next'><a onclick = followershow("+(pageidx+1)+")>"+nexpagetext[locale]+" &rarr;</a></li></ul>");
				if(pageidx > 0) {
					$(".next").before("<li class='previous'><a onclick = followershow("+(pageidx-1)+")>&larr; "+prepagetext[locale]+"</a></li>");
				}
			}
	});
}

function pfollowershow(pageidx){
	followershow(pageidx);
	gtabsel = 'pfolshw';
}

function adjustcircle(o) {
	var adjustcircle ='';
	var adjustcircletext = new Array(
		'Adjust group',
		'调整圈子',
		'Ajuste grupo',
		'グループを調整します'
	);

	if(gtabsel == 'circlepost'||gtabsel == 'circlepeople'){
		adjustcircle = "<a  class='btn btn-success btn-small pull-right' style='padding-right:20px;' id=" + getpersionid(o) 
		               + "requestmodfriend style='text-align: right'  onclick=updatecirclesset(this.getAttribute('id'))>"+adjustcircletext[locale]+"</a>";
    }
   
	return adjustcircle;
}

function removefromcircle(o) {
	var rmcircle ='';
	var rmcircletext = new Array(
		'Delete ',
		'取消关注',
		'excluir',
		'削除する'
	);

	if(gtabsel == 'circlepost'||gtabsel == 'circlepeople'){
		rmcircle = "<a  class='btn btn-warning btn-small pull-right' id=" + getpersionid(o)
		           + "requestrmfriend style='text-align: right' onclick=removecirclesset(this.getAttribute('id'))>"+rmcircletext[locale]+"</a>";
    }
   
	return rmcircle;
}

function ishisfriend(o) {
	var ishisfriend ='';
	var inde = false;

  	var yourfriendtext = new Array(
  		'Your friend ',
  		'互为好友',
  		'o seu amigo',
  		'あなたの友人 '
  		);
		
	for(var n = 0; n < friendmenber.length; n++) {
		if(friendmenber[n] == getpersionid(o)) {
			inde = true;
		}
	}
	
	if(getpersionid(o) == $.cookie('user_id')) {
		inde = true;
	}
	
	if(!inde) {
		if(gtabsel == 'public'||gtabsel == 'folshw'||gtabsel == 'pfrishw'||gtabsel == 'pfolshw'||gtabsel == 'searchuser'){
		    ishisfriend = "<a  class='btn btn-warning btn-small pull-right' id=" + getpersionid(o) + "requestaddfriend style='text-align: right' onclick=friendcirclesset(this.getAttribute('id'))>"+addfriendtext[locale]+"</a>";
		}
	}
	
	else{
		if(gtabsel == 'public'||gtabsel == 'folshw'||gtabsel == 'pfrishw'||gtabsel == 'pfolshw'||gtabsel == 'searchuser'){
		   ishisfriend = "<div  class='btn btn-success btn-small pull-right' id=" + getpersionid(o) + "requestaddfriend style='text-align: right'>"+yourfriendtext[locale]+"</div>";
		}
	}

	return ishisfriend;
}


function friendcirclesset(id){
	
	var usrid = id.substring(0, id.indexOf('requestaddfriend'));
	
	gccidlist.length = 0;	
   
	if($('#phone-top').css("display") !== "block") {
	    //circles list for selection
        $("#dlgcircles").dialog('option',
    	                        'buttons',
    	                        {
    	                       	 'OK':function()
    	                             {
    	                             	AddUserAsFriend(gccidlist, usrid);
    	                             	$(this).dialog("close");
    	                             	return true;	                 	             
    	                             }  ,
                                 'Cancel':function()
    	                             {
    	                             	$(this).dialog("close");
  	                 	             }     	                                            	     
    	                 	     }  	                    	                 
    	                      ); 
    	                 	
	    //打开对话框显示圈子列表
        showCircleNameList("",
                           function(){$("#dlgcircles").dialog("open");}
                          );
        			
	}else {	 
	     //add to default circle				
         AddUserAsFriend('6', usrid);        		
	}
}

function getlastmessage(user_id){
	var guangbo='';
	var lastmessagetext = new Array(
		'Latest message ',
		'最近广播 ',
		'Durar a mensagem ',
		'最後のメッセージ '
	);
	
	invokeApi("post/userstimeline", prepareData({
		'users' :user_id,
		'count' : '1'
	}), function(json){
		$.each(json, function(i, o) {
		var lastmessage="<div class='lastmessage' id="+getpostid(o)+">"
		+"<div class='msgCnt'><br>"
		+"<div style='color: silver;'>"
		+lastmessagetext[locale]
		+unix2human(getmessge_create_time(o))
		+"</div>"
		+		getmessge(o)
		+"</div>"
		+"	<div class='mediaWrap' >"
		+	getrepost(o)
		+"		<div class='share'>	"
		+			getshareinfo(o)
		+"		</div>	"
		+"	</div>"

		+"</div>	";
		$("#"+user_id+"status").append(lastmessage);
		});
		//alert(JSON.stringify(json[message]));
			}) ;
	return guangbo;
	
}
function getsuggest(){	
	beforeload();
	var nosuggesttext = new Array(
		'<li>No Suggest</li>',
		'<li>暂时没有推荐收听</li>',
		'<li>Não. Sugerir,</li>',
		'<li>推奨しない</li>'
	);
	invokeApi("suggest/get", prepareData({
		'count' : '100'
	}),function(json){
		document.getElementById("suggest").innerHTML = "";
		if(json==''){			
			document.getElementById('suggest').innerHTML = nosuggesttext[locale] ;
			afterload();	
		}
		else{
				$.each(json, function(i, o) {
					if(i<4){
						$("#suggest").append("<div  id="+o.user_id+"suggest >"
						+"<div style=\"margin-left:0px\" class=\"span1\"><img src=\""+o.image_url+"\" onload=resizeimg(this,50,50) ></div>"             				
						+"<div class=\"user_info\">"
						+		getdisplayname(o)+"<br>"
						+"		<a id="+o.user_id+"requestaddfriend style='text-align: right' onclick=addsuggest(this.getAttribute('id'))>"+addfriendtext[locale]+"</a><br>"
						+"		<p>"+getreason(o)+"</p>"
						+"</div></div>"				
						);
					}
					else{					
						$("#suggest").append("<div class='hiddensuggest' style='display:none'  >"
						+"<div style=\"margin-left:0px\" class=\"span1\"><img src=\""+o.image_url+"\" onload=resizeimg(this,50,50) ></div>"             				
						+"<div class=\"user_info\">"
						+		getdisplayname(o)+"<br>"
						+"		<a id="+o.user_id+"requestaddfriend style='text-align: right' onclick=addsuggest(this.getAttribute('id'))>"+addfriendtext[locale]+"</a><br>"
						+"		<p>"+getreason(o)+"</p>"
						+"</div></div>"				
						);
					}
				});
				if(json.length>5){
					$("#suggest").append("<li class='hiddensuggest' style='display:none'><a class='more pull-right' onclick='showlesssuggest()'>"+lesstext[locale]+"</a></li>");
					$("#suggest").append("<li class='showsuggest'><a class='more pull-right' onclick='showmoresuggest()'>"+moretext[locale]+"</a></li><hr style='margin-top:0px'>");

				}

		afterload();
	}
		
	});
}
function getreason(o){
	var reason='';
	if(o.suggest_type=='10'){
		reason=new Array(o.suggest_reason.display_name+'suggest',o.suggest_reason.display_name+"推荐收听",o.suggest_reason.display_name+'sugerir',o.suggest_reason.display_name+'提案する');	;					
	}else if(o.suggest_type=='12'){
		reason=	new Array('Request from '+o.display_name,o.display_name+"请求收听",'Pedido de'+o.display_name,'からの要求'+o.display_name);	
	}else if(o.suggest_type=='21'||o.suggest_type=='23'||o.suggest_type=='50'){
		reason=new Array('System suggest ','系统推荐收听','o Sistema de sugerem','システムを推奨');		
	}else if(o.suggest_type=='31'){
		reason=new Array('Your colleagues ','您的同事','Seus colegas','あなたの同僚');
	}else if(o.suggest_type=='32'){
		reason=new Array('Your classmates','您的同学','Seus colegas de classe','クラスメート');	
	}else if(o.suggest_type=='40'||o.suggest_type=='22'){
		reason=	new Array('Hava same friends','你们有共同的好友','Você tem os mesmos amigos','あなたは、同じ友人がいない');
	}
	return reason[locale];
}
function showmoresuggest(){
	$(".hiddensuggest").css({
		display: ""
	});
	$(".showsuggest").css({
		display: "none"
	});	
}
function showlesssuggest(){
	$(".hiddensuggest").css({
		display: "none"
	});
	$(".showsuggest").css({
		display: ""
	});
}

function addsuggest(id){

	gtabsel = 'sugshw';
	friendcirclesset(id);	
}

function postAction()
{
	
	if($.cookie('user_id') === '10471') {
		window.location = 'register.html';
		return;
	}
	
	var namelist = new Array();
	namelist = $("#recadd").val().split(",");
	var sendidlist = '';
	
	for(var i = 0;i<namelist.length;i++){
		for(var j = 0;j<usidlist.length;j++){						
			if(namelist[i].length < 1)
				break;
			if(namelist[i] == usidlist[j][0])
			{
				sendidlist=sendidlist+usidlist[j][1]+",";
				break;
			}
		}
	}
	/*for(var i = 0;i<gidlist.length;i++){
		sendidlist=sendidlist+gidlist[i]+",";
	}*/
	var locationstring=$("#locationpreview").html();
	var onpc =$("#photo_image").is(":visible");
	if(onpc == true){
		if (/^[\s'　']*$/.test($("#myPost").val())&&(locationstring.length==0)&&($("#photo_image").val().length == 0)){				
			alert(nulltext[locale]);
			return;
		}
	}
	else{
		if (/^[\s'　']*$/.test($("#myPost").val())&&(locationstring.length==0)){				
			alert(nulltext[locale]);
			return;
		}
	}	
		
	locationstring=locationstring.substring(0,locationstring.indexOf('<a'));
	var msg='';
	if(locationstring.length==0)
		msg = $("#myPost").val();	
	else
		msg = $("#myPost").val()+"<br>位置："+locationstring;
	
	//私密方式发布
	var secretly = '';
	if($('#bprivate').attr('checked'))
		secretly = true;
	else
		secretly = false;	
	
	if(onpc == true){
		document.postForm.appid.value = "1";
		document.postForm.ticket.value = $.cookie('ticket');
		document.postForm.mentions.value = sendidlist;	
		document.postForm.msg.value = msg;
		document.postForm.secretly.value = secretly;		
		
		var signstr ="";
		signstr = getSignData("msg","photo_image","mentions","secretly");
		document.postForm.sign.value = signstr;
		
		var options ={			        
		 	    type:      'POST',       
		        dataType:  'json',         
		        url:qiupuip+'post/create',
		        beforeSubmit:showRequest,
		        success:callbacksucc,
		        error:callbackfail              
		        };
			$('#postForm').ajaxSubmit(options);	
			return false;     
		
	}
	else{			
		//on the phone,use invokeapi,because cannot get the callback use ajaxsubmit		
		invokeApi("post/create", prepareData({'mentions':sendidlist,'secretly':secretly,'msg':msg}),function(ret) {
			if (ret["error_msg"] != undefined ) 
				callbackfail();
			else
				callbacksucc();
		});
	} 
	$("#postcontent").addClass("hideOnPhone");
	$("#recadd").addClass("hideOnPhone");
	$("#content").removeClass("hideOnPhone");
	   			          
}

function showRequest(formData, jqForm, options) {     
    var queryString = $.param(formData); 			 
    //alert('About to submit: \n\n' + queryString); 			   
    return true; 
} 
function callbacksucc(json)
{	
	clearinput();
	refreshed(gtabsel,0);
	//not default in all
	//friendtimeline(i=0);   
	//window.location = "index.html";		
}
function preview(){	
	var x = document.getElementById("photo_image");
	var imagevalue="";     	
	if(!x || !x.value) return;
	getFullPath(x);
	var patn = /\.jpg$|\.jpeg$|\.gif$|\.png$/i;
	if(x.value.length<9){
		imagevalue = x.value;
	}else{
		imagevalue = x.value.substring(0,6)+"..."+x.value.substring((x.value.length-3),x.value.length);   
	}
	//使用正则判断用户选择的文件类型
	if(patn.test(x.value)){
		if(window.navigator.userAgent.indexOf("Chrome") >= 1){ 			
			document.getElementById("photopreview").innerHTML="<a class=\'inline\'>"+imagevalue+"</a><a onclick=\'deleteFile(\"photo_image\")\'>×</a>";
		} 
		else{
			var img=document.createElement('img');    //创建图像文件，并设置图像的高度、宽度和id
			img.setAttribute('src',getFullPath(x));
			img.setAttribute('width','20');
			img.setAttribute('height','20');
			img.setAttribute('id','img1');
			$("#photopreview").append(img);	
			$("#photopreview").append(imagevalue+"<a onclick='deleteFile(\"photo_image\")'>×</a>"); 
		}
		}
	else{ alert("您选择的不是图像文件。"); }
	
		
}
function getFullPath(obj) { 
        if (obj) { 
            //ie 
            if (window.navigator.userAgent.indexOf("MSIE") >= 1) { 
                obj.select(); 
                return document.selection.createRange().text; 
            } 
            //firefox            
            else if (window.navigator.userAgent.indexOf("Firefox") >= 1) { 
                if (obj.files) { 
                	var objectURL = window.URL.createObjectURL(obj.files[0]);
                	return objectURL; //obj.files.item(0).getAsDataURL(); 
                } 
                return obj.value; 
            } 
            return obj.value; 
        } 
    } 
//file,input type=file 的name
function deleteFile(file){
	$("#photopreview").html("");
	var ie = (navigator.appVersion.indexOf("MSIE")!=-1);//IE 
	var ff = (navigator.userAgent.indexOf("Firefox")!=-1);//Firefox 
	if(ie)
    	refreshUploader($("input[name="+file+"]")[0]);
	else
		$("input[name="+file+"]").attr("value",""); //FF
}

function clearinput(){
	$('#myPost').val('');//clear the input box	
	$('.mf_list').html("");//clear the name		
	gidlist.length =0;
	gtmpidlist.length =0;	
	gnamelist.length =0;	
	gtmpnamelist.length =0; 
	$("#bprivate").attr("checked",false);
	if((!document.getElementById("photo_image").value)){
		
	}else{
		deleteFile("photo_image");
	}
	$('#locationpreview').html("");
}
function callbackfail(json)
{
	//setTimeout(alert(JSON.stringify(json)),10000);
	clearinput();
	friendtimeline(0);//update publictimelinegidlist.length =0;
   	window.location = "home.html";
}

function startntf()
{
	nowtime = Math.round(new Date().getTime());
	starttime = nowtime - 180000;			
	var timer_timeout = null;
	var func = function() {		
	    invokeApi("post/qiupufriendtimeline", 
			   prepareData({'start_time':starttime,'count':'10000','cols':'post_id'}),
			   function(json) {
			   		  if (json != '' ) {				      			
					    if(json.length>0)
					    {
					    	gneedfresh="friend";
					    	//alert("count is: "+json.length);
					    	$('#ntf').show();	
					    	starttime = Math.round(new Date().getTime());	
					  		timer_timeout = setTimeout(func, 300000);
					    }    
				      }	
				      else//qiupufriendtimeline为空
				      {
				      	invokeApi("post/qiupupublictimeline", 
							   prepareData({'start_time':starttime,'count':'10000','cols':'post_id'}),
							   function(json) {
							   		  if (json != '' ) {								      			
									    if(json.length>0)
									    {
									    	gneedfresh="public";
									    	//alert("count is: "+json.length);
									    	$('#ntf').show();	
									    }    
								      }	
								      starttime = Math.round(new Date().getTime());	
					  				  timer_timeout = setTimeout(func, 300000);
				      	
				      	});
				      }				      
	          });	    
	};
	func();   

}


function iscanProcess(vtype) {
	var bcanPro = false;
	
	if(vtype=='ntf.new_follower' 
	    || vtype=='ntf.app_share'
	    || vtype=='ntf.other_share'
	    || vtype=='ntf.profile_update'
	    || vtype=='ntf.create_account' 
	    || vtype=='ntf.my_stream_comment'
	    || vtype=='ntf.my_stream_retweet'
	    || vtype=='ntf.my_app_comment') {
	  bcanPro = true;	    	
	}
	  
	return bcanPro;
}

var func = function(){
		var timer_timeout=null;			
	    var mcount=0;	
	    invokeNotifiApi("list.jsonp",
	                    {'status' : '0','size':-1 }, 
			            function(json) {		                				                	
				            var jsonf = json.result;
				            if((jsonf !== undefined) && (jsonf.count > 0)) {
				       	         var jsonv = jsonf.informations;
				                 $.each(jsonv, function(i, o) { //skip notification type will not processed in web
				                      if(iscanProcess(o.type))  ++mcount;
				                 });
			                  	  
			                  	 if(mcount>0) {		                  	   	
			                          $('#ntfcnt').html(JSON.stringify(mcount));			                          
			                          $('#ntfli').show();			                          
			                          $('#ntfcnt').show();
			                          $('#pntfcnt').html(JSON.stringify(mcount));			                          
			                          $('#pntfli').show();			                          
			                          $('#pntfcnt').show();
			                          $('#pdntfcnt').html(JSON.stringify(mcount));			                          
			                          $('#pdntfli').show();			                          
			                          $('#pdntfcnt').show();			                          			                          		                          
			                  	 }  else {
			                  	 	$('#ntfcnt').hide(); 
			                        $('#ntfli').hide();
			                        $('#pntfli').hide();
			                        $('#pntfcnt').hide();
			                        $('#pdntfli').hide();
			                        $('#pdntfcnt').hide();                 	 	
			                  	 }  
			                  	   		                  
					  		     timer_timeout = setTimeout(func,30000);
					  		} else {
			                  	 	$('#ntfcnt').hide(); 
			                        $('#ntfli').hide();
			                        $('#pntfli').hide();
			                        $('#pntfcnt').hide();
			                        $('#pdntfli').hide();
			                        $('#pdntfcnt').hide();					  			
					  		}		                				                			                  			      	  			      	
			            });	
};

function startnotify()
{						
	func();			 
}

function markAsRead(id) 
{		
	 var mid=id.substring(0,id.indexOf('ntfxid'));   
	 invokeNotifiApi("doneget.jsonp",
	                {'mid' :  mid},  
	                function(json) {
	                   var jsonf=json.result;
	                   if(jsonf !== undefined) {
	                   	  if(jsonf.status === 'success') {
	                   	     $('#'+mid+'ntfxid').removeClass('nfxbold');		                   	  	
	                   	  }                  	  
	                   	  startnotify();  	                   	                       	    
	                }	
	}); 
}


function getmsgprocessed(o) {

    var messages=o;
	var reg1 = /(borqs:\/\/profile\/details\?uid=)/g;    
	var reg2 = /(borqs:\/\/application\/details\?id=)/g; 
	var reg21 = /(\-arm)/g; 
	var reg22 = /(\-x86)/g; 		   
	var reg3 = /(href=\"borqs:\/\/stream\/details\?id=)/g; 
	var reg4 = /(href=\"borqs:\/\/stream\/comment\?id=)/g;
	var reg5 = /(\+)/g;
				
    if(messages !== undefined && messages !== null && messages !== '') {
	   if(messages.indexOf('borqs://profile/details?uid=')>=0){  //go to the person index
		  messages=messages.replace(reg1,'persionindex.html?persionid=');
		  if(messages.indexOf('+')>=0){
		  	messages=messages.replace(reg5,'@');
		  }
	   }
	   if(messages.indexOf('borqs://application/details?id=')>=0){ //show the static download url
		  messages=messages.replace(reg2,'http://static-apk.borqs.com/apk/');
	   }
	   
	   if(messages.indexOf('-arm')>=0){   //add extend-name
		  messages=messages.replace(reg21,'-arm.apk');
	   }
	   if(messages.indexOf('-x86')>=0){   //add extend-name
		  messages=messages.replace(reg22,'-x86.apk');
	   }	
	      	   
	   if(messages.indexOf('href=\"borqs://stream/details?id=')>=0){
	   	  messages=messages.replace(reg3,' id=\"'+objectId+'modal\" style=\"color:orange\" data-toggle=\"modal\" href=\"#');	
	      picposthref="";	   
	   }
	   
	   if(messages.indexOf('href=\"borqs://stream/comment?id=')>=0){
	   	  messages=messages.replace(reg4,' id=\"'+objectId+'modal\" style=\"color:orange\" data-toggle=\"modal\" href=\"#');	  
	      picposthref="";
	   }	 
	   
	   for(var i=0;i < resMsgNotifytext[1].length;i++) {
	   	  var k =$.cookie('locale');
	      messages=messages.replace(resMsgNotifytext[1][i],resMsgNotifytext[k][i]); 	   	  
	   }
 	   
	   	   
	   return messages;	       	
    }
    else return '';

}

//list resolved notifications
function listnotificationresovled(func){
	
	var originaltitle = new Array(
		              'View the original message',
		              '查看原信息',
		              'Ver a mensagem original',
		              '元のメッセージを表示'
	                  );		
	invokeNotifiApi("top.jsonp",
	               {'status' : '1','topn':20}, 
		            function(json) {
				       var jsonf = json.result;
				       if(jsonf.count>0) {
				       	    var jsonv = jsonf.informations;
				            $.each(jsonv, function(i, o) {
				                if(iscanProcess(o.type)){ //skip the request for change vcard
				                	objectId=getnotification_postid(o);
				                	picposthref="<a style='color:orange' id=\""+objectId+"modal\" data-toggle=\"modal\" href=\"#"
				                	           +objectId+"\">"+originaltitle[locale]+"</a>";
				                	if(o.type=='ntf.my_stream_comment'){
					                	$("#nfxlines").append("<div class='row' id="+o.id+"ntfxbody >" 
						                + "<div class='span5 myspan5' >"
						                + getmsgprocessed(o.titleHtml)
						                + picposthref
						                + "<br>"
						                + getmsgprocessed(o.bodyHtml)					                
						                + "</div>"
							            + "</div><hr>");
							            notification_for_stream(objectId);		
				                	}
				                	else{
				                		$("#nfxlines").append("<div class='row' id="+o.id+"ntfxbody >" 
						                + "<div class='span5 myspan5' >"
						                + getmsgprocessed(o.titleHtml)
						                + "<br>"
						                + getmsgprocessed(o.bodyHtml)
						                + "</div>"
							            + "</div>"
							            + "<hr>");
				                	}
			                	
				                };

				            });				       	
				       }
                    func();
	});
}

//list read notifications
function notificationread(){
	gtabsel = 'nfxshowread';
	$('#maincontent').hide();
	$('#usercontent').hide();
	$('#nfxcontent').show();	
	
	//for top menu on phone
	$('#index-notice').addClass('active');
	$('#publictop').removeClass();	
	$('#friendtop').removeClass();
	
	var originaltitle = new Array(
		              'View the original message',
		              '查看原信息',
		              'Ver a mensagem original',
		              '元のメッセージを表示'
	                  );	
	                  
	document.getElementById('nfxlines').innerHTML = "";	 
	                 	
	beforeload();
	invokeNotifiApi("top.jsonp",
	               {'status' : '0','topn':50 }, 
		            function(json) {
				       var jsonf = json.result;
				       if(jsonf.count>0) {
				       	    var jsonv = jsonf.informations;
				            $.each(jsonv, function(i, o) {
				                if(iscanProcess(o.type)){//skip the request for change vcard
				                	objectId=getnotification_postid(o);
				                	picposthref="<a style='color:orange' id=\""+objectId+"modal\" data-toggle=\"modal\" href=\"#"
				                	            + objectId + "\">" + originaltitle[locale]+"</a>";				                	
				                	if(o.type=='ntf.my_stream_comment'){
				                		$("#nfxlines").append("<div class='row nfxbold' id="+o.id+"ntfxid onclick=markAsRead(this.id)>" 
						                +"<div id="+o.id+"ntfxbody class='span5 myspan5' >"
						                + getmsgprocessed(o.titleHtml)
						                + picposthref
						                + "<br>"
						                + getmsgprocessed(o.bodyHtml)
						                + "</div>"
							            + "</div><hr>");
							            notification_for_stream(objectId);	
					                	}
					                else{
					                	$("#nfxlines").append("<div class='row nfxbold ' id="+o.id+"ntfxid  onclick=markAsRead(this.id)>" 
						                +"<div id="+o.id+"ntfxbody class='span5 myspan5' >"
						                + getmsgprocessed(o.titleHtml)
						                + "<br>"
						                + getmsgprocessed(o.bodyHtml)
						                + "</div>"
							            + "</div><hr>");
					                }			                	
						        }
				            });   	
				       }			       
				       listnotificationresovled(afterload());
	});
	
}
function getnotification_postid(o){
	var postid='';
	if (o.objectId===''||o.objectId===undefined){
		if(o.uri!==''||o.uri!==undefined){
			postid=o.uri.substring(o.uri.indexOf('id=')+3); 
		}
	}
	else{
		postid=o.objectId;
	}
	return postid;
}
function notification_for_stream(s){
	var id=s;
	var parent_stream;
	
	var deletetitle = new Array(
		              'The original message has been deleted',
		              '原消息已删除',
		              'A mensagem original foi apagado',
		              '元のメッセージは削除されました'
	                  );
	
	invokeApi("post/qiupuget", prepareData({
		'postIds' : id
	}),function(json){
		if(json.length==0){
			parent_stream="<div style='color:black'  id=\""+id+"\" class=\"modal hide fade \" >"
			+	"<div class='modal-header'>"	
			+		"<a class=\'close\' data-dismiss=\'"+id+"modal\'>&times;</a>"
			+	"</div>"
			+	"<div class='modal-body' style='text-align:center'>"
			+		"<p>"
			+		deletetitle[locale]
			+		"</p>"		
			+	"</div>"
			+"</div>";			
			$("#"+id+"modal").append(parent_stream);
		}
		else{
			$.each(json, function(i, o) {
				parent_stream="<div style='color:black' id=\""+id+"\" class=\"modal hide fade\">"
				+	"<div class='modal-header' style='text-align:center;font-size: 14px;'>"
				+	getdisplayname(o)+gettopeople(o)
				+		"<a class=\'close\' data-dismiss=\'"+id+"\modal'>&times;</a>"
				+	"</div>"
				+	"<div class='modal-body' style='text-align:center'>"
				+		"<p>"
				+		getmessge(o)+getrepost(o)+getshareinfo(o)
				+		"</p>"
				+		"<div id='"+getpostid(o)+"public' class='public' style='text-align:left' >"
				+		"</div>"			
				+	"</div>"
				+"</div>";				
				$("#"+id+"modal").append(parent_stream);
				invokeApi('comment/for', {'target' : id,'count':'-1'}, function(ret) {
					$("#"+getpostid(o)+"public").append(comment(ret));				
				});	
			});
		}				
		});		
		return parent_stream;
}

function gotoTop(){
	
	var gotoTopTitle = new Array(
		'Back To Top',
		'返回顶部',
		'Voltar ao topo',
		'トップへ戻る'
	);
	
    //预定义返回顶部的html代码，它的css样式默认为不显示
    var gotoTop_html = gotoTopTitle[locale];
    //将返回顶部的html代码插入页面上id为page的元素的末尾 
    $("#gotoTop").html(gotoTop_html);
    $("#gotoTop").click(//定义返回顶部点击向上滚动的动画
        function(){$('html,body').animate({scrollTop:0},1000);
    });
};
function postshow(){
	$("#postcontent").toggleClass("hideOnPhone");
	$("#content").toggleClass("hideOnPhone");
	$("#recadd").toggleClass("hideOnPhone");
	$("li").removeClass('active');
    history.pushState({state: 'Phone_post_show'}, '', '');
	if($("#postcontent").hasClass("hideOnPhone")){
		$("#myPost").css({"height":'auto'});
		$("#postcontent").css({"margin-top":'0px'});
		$("#index-post").attr("src","style/images/title_new_normal.png");
		
	}
	else{
		$("#index-post").attr("src","style/images/title_new_selected.png");
		$("#postcontent").css({"margin-top":'75px'});
		if(window.innerHeight){
			var h = (window.innerHeight-200)+'px';
			$("#myPost").css({"height":h});
		}
		else{
			$("#myPost").css({"height":'200px'});
		}	
		
	}
	
}

window.onpopstate = function(event) {
	if(event.state === {
		state : 'Phone_post_show'
	}) {
		$("#postcontent").toggleClass("hideOnPhone");
		$("#recadd").toggleClass("hideOnPhone");
		$("#content").toggleClass("hideOnPhone");
	} else {
		$("#postcontent").addClass("hideOnPhone");
		$("#recadd").addClass("hideOnPhone");
		$("#content").removeClass("hideOnPhone");
		$("#index-post").attr("src","style/images/title_new_normal.png");		
	}
}

var usidlist = new Array();
//显示用户的好友列表
function GetFriends()
{	
	
	var i = 0;	
	invokeApi("friend/show",prepareData({'page':0, 'count':9999}),
      function(json){  
      	  var friendname1 = new Array();                	      	
          $.each(json, function(i, o) {	  
          	 friendname1[i] = new Array();       	
          	 friendname1[i][0] =o.display_name;
          	 var pyname = CC2PY(o.display_name);
          	 //alert(pyname);	 
          	 friendname1[i][1] = pyname;
          	 friendname1[i][2] = o.user_id;
          	 
          	 
          });
          var num = friendname1.length;
          $("#recadd").autocomplete(friendname1, {
          	max: num,    //列表里的条目数					
          	minChars: 0,
			multiple: true,
			mustMatch: false,
			autoFill: false,
			matchContains: true,
			scroll: true,	
			formatItem: function(row, i, max) { 
             	//下拉列表显示条目内容
             	return row[0];  
            }, 
             formatMatch: function(row, i, max) { 
             	//alert(row[1] + row[0]);//搜索范围
             	return row[1] + row[0];
             
             }, 
             formatResult: function(row) { 
             	//返回结果内容
                 return row[0]; 
             } 
            	            			
			});
			$("#recadd").result(function(event, data, formatted) {
				if (data) {
			        //record the name and usrid
					usidlist[i] = new Array();     
					usidlist[i][0] = data[0];
					usidlist[i][1] = data[2];
			        i++;
			    }
			    

			});					              
	});
}	

function setlanguage(localeId){
	if (localeId === $.cookie('locale')){
		$('.dropdown-menu').toggle();
		return;
	}else{
		locale = localeId;//global variable
		$.cookie('locale', localeId, { expires: 9999 });
		$('#menutabs li').each(function(i,o) {
   	    	$(this).html(resPhoneTimeLineTabs[locale][i]);
    	});
    	//Phone- Top navigation
    	$('#phonenavmain').html(resPhoneNavTitle[locale]);
    	$('.todaytxt').html(todaytext[locale]);
    	$('.deletcom').html(deletext[locale]);
    	$('.comtext').html(comtext[locale]);
    	$('.repostext').html(repostext[locale]);   	 
    	$('.dropdown-menu').toggle();    	
	}
	
}
function publictime(pageidx) {
	fun = 'publictime';
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	gpageidx = pageidx;
    gtabsel = 'public';	
	beforeload();
	invokeApi("post/qiupupublictimeline", {
		'appid' : '1',
		'page' : pageidx
	}, function(json) {
			if(json==''){
			afterload();
			if(pageidx==0){
				document.getElementById('timelines').innerHTML = "";
			}else{
				alert(resLastPage[locale])
				pageidx=pageidx-1;}
			}
			else{			
				document.getElementById('timelines').innerHTML = "";
				$.each(json,function(i,o) {
			var list2="<div class='row' id="+getpostid(o)+"u"+getpersionid(o)+">"  //style='border:1px solid #DDD' 
			+"<div class='hideOnPhone span1' style=\"width:48px\" >"
			+"<a  class='hideOnPhone'  >"
			+"<img src=" +geturspic(o) + " onload=resizeimg(this,48,48)>"
			+"</a>		"
			+"</div>"
			+"<div class='span5 myspan5' >"
			+"<div class='msgcnt' >"
			+"	<div class='msgbox'>"
			+"		<div class='userName' >"
			+				o.from.display_name
			+"		</div>"	
			+"	</div>"
			+		getmessge(o)			
			+"	<div class='mediaWrap' >"
			+	getrepost(o)
			+"		<div class='share'>	"
			+			getshareinfo(o)
			+"		</div>	"
			+"	</div>"
			+"</div>"
			+"	<div class='pull-left' style='color: silver;'> "
			+"		"+unix2human(getmessge_create_time(o))
			+"	"+getdevice(o)
			+"	</div>"
			+"</div>"
			+"</div><hr>";
			$("#timelines").append(list2);	
				});
				afterload();
				showpage(pageidx,fun,gpnexttimes);
			}
	});
	}
