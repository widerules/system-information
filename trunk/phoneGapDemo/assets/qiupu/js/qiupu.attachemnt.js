//attachemnt show
function getshareinfo(o) {
	var shareinfo;
	if (o.attachments.length==0||o.type == 1||o.attachments===undefined){ //this is text_post
		shareinfo = "";
	}
	else if (o.type ==2){ //this is photo_post 
		/*photo_post_example:		 
		"album_id" : "2797522207669083081",
    	"album_name" : "User default Album",
   		"album_photo_count" : "14",
   		"album_cover_photo_id" : "2798199990942321450",
    	"album_cover_photo_middle" : "http://apitest.borqs.com/photo/42_2797522207669083081_20120413105531_O.jpg",
    	"album_cover_photo_big" : "http://apitest.borqs.com/photo/42_2797522207669083081_20120413105531_L.jpg",
    	"album_cover_photo_small" : "http://apitest.borqs.com/photo/42_2797522207669083081_20120413105531_S.jpg",
    	"album_description" : "",
    	"album_visible" : "1",
    	"photo_id" : "2798199990942321450",
    	"photo_img_middle" : "http://apitest.borqs.com/photo/42_2797522207669083081_20120413105531_O.jpg",
    	"photo_img_big" : "http://apitest.borqs.com/photo/42_2797522207669083081_20120413105531_L.jpg",
    	"photo_img_small" : "http://apitest.borqs.com/photo/42_2797522207669083081_20120413105531_S.jpg",
    	"photo_caption" : "IMG_0890",
    	"photo_location" : "",
    	"photo_tag" : "",
    	"photo_created_time" : "1334285734731",*/
    	var photo_img_small ;
    	var photo_img_middle;
    	var photo_tag;
    	
    	for(var key in o.attachments) {
    		photo_img_small = o.attachments[key].photo_img_small;
    		photo_img_middle = o.attachments[key].photo_img_middle;
    		photo_img_big = o.attachments[key].photo_img_big;
    		photo_tag = o.attachments[key].photo_tag; 
    		photo_caption = o.attachments[key].photo_caption;
    	}
    	
    	if(o.message){
    		photo_caption = '';
    	}
    	else{
    		photo_caption = photo_caption;
    	}
    	
    	var photoid = o.post_id_s+"photo";
  		var imgids = o.post_id_s+"imgs";
  		var imgidl = o.post_id_s+"imgl";
  		var sharetext = new Array(
  			'Share pictrues : ------',
  			'分享图片：------',
  			'Compartilhe fotos： ------',
  			'シェアの写真： ------'
  		);

  		shareinfo =photo_caption+"<p style='color: silver;'>"+sharetext[locale]+"<br></p><a class='miniImg artZoom' href='"+photo_img_middle+"' rel='"+photo_img_big+"'><img style=\"width:150px;\" src='"+photo_img_small+"'></a>"
		
  		/*shareinfo =photo_caption+"<p style='color: silver;'>分享图片：------<br></p><a class='miniImg artZoom' href='"+photo_img_middle+"' rel='"+photo_img_big+"'><img style=\"width:150px;\" src='"+photo_img_small+"'></a>"
	   	shareinfo = "<p style='color: silver;'>分享图片：------<br></p>"
    		+"<div id=\""+photoid+"\"  title=\""+photo_tag+"\">"
    		+"	<img class='well' onclick=photoclick(this) id=\""+imgids+"\" style=\"width:150px;\" src=\""+photo_img_small+"\" alt=\""+photo_tag+"\"/>"
    		+"	<p class='well' id=\""+imgidl+"\" class='span4' style=\"display: none\">"
    		+"		<a onclick=\"$('#"+imgidl+"img').rotateLeft(90);\">左转</a>"
    		+"		<a onclick=\"$('#"+imgidl+"img').rotateRight(90);\">右转</a>"
    		+"		<img id=\""+imgidl+"img\"  onclick=photoclick(this) src=\""+photo_img_middle+"\"/>"
    		+"	</p></div>";*/
		
	}
	else if (o.type ==16||o.type ==2048){ //this is book_post
		/*book_post_example
		"id" : "611__-38192347402877790692716298323180126762__276__",
    	"summary" : "失恋33天",
    	"coverurl" : "http://booktest1.borqs.com:8980/brook/cover.borqs?name=%E5%A4%B1%E6%81%8B33%E5%A4%A9&ownerid=276&md5=-38192347402877790692716298323180126762",
    	"name" : "失恋33天"*/    	
    	var summary;
    	var name;
    	
    	for(var key in o.attachments){
    		summary = o.attachments[key].summary;
    		name = o.attachments[key].name;
    	}
    	if(summary.length<=0){
    		var sum= name;
    	}
    	else{
    		var sum= summary;
    	}
    	var sharetext = new Array(
  			'Share books : ------',
  			'分享图书：------',
  			'Compartilhamento de livros： ------',
  			'共有ブック： ------'
  		);
    	shareinfo = "<p style='color: silver;'>"+sharetext[locale]+"<br></p><p class='well'>" +sum+ "</p>";
	}
	else if (o.type ==32){ //this is apk_post
		/*apk_post_example
		"package" : "game.destiniaeng",
	    "app_name" : "DESTINIA",
	    "version_code" : 106,
	    "version_name" : "1.0.6",
	    "architecture" : 1,
	    "target_sdk_version" : 7,
	    "category" : 512,
	    "sub_category" : 513,
	    "created_time" : 1329747320188,
	    "info_updated_time" : 1332707039327,
	    "description" : "\t\t\t\t\t\t这是一款相当不错的RPG类游戏，来自Gamevil公司开发，整体来说，相当不错。 6M的超小容量，但是不俗的操作感，流畅的打击感，丰富的内容，特别是加入了宠物系统，使游戏更加的吸引人。<br><br>DESTINIA拥有持续时间超过30小时的迷人的情节，三种级别可供选择，并可以学习39中技能。强硬的战斗是迅速的，结合美妙的画面形成了高品质的RPG动作体验。一个灵活的理念加上宠物进化系统为升级添加了新的自由和深度，并会为GAMEVIL的fans带来新的挑战。<br><br>v1.0.5更新内容：<br>- 一些小错误修正",
	    "recent_change" : "",
	    "rating" : 4.0,
	    "upload_user" : {
	      "user_id" : 10121,
	      "display_name" : "李小佳",
	      "image_url" : "http://api.borqs.com/sys/icon/1.gif",
	      "address" : [ ]
	    },
	    "screen_support" : null,
	    "icon_url" : "http://api.borqs.com/apk/game.destiniaeng-106-arm.icon.png",
	    "price" : 0.0,
	    "borqs" : 0,
	    "market_url" : "",
	    "file_size" : 6538048,
	    "file_url" : "http://static-apk.borqs.com/apk/game.destiniaeng-106-arm.apk",
	    "tag" : "",
	    "screenshots_urls" : [ "http://api.borqs.com/apk/game.destiniaeng-106-arm.screenshot1.1332707039325.jpg", "http://api.borqs.com/apk/game.destiniaeng-106-arm.screenshot2.1332707039325.jpg", "http://api.borqs.com/apk/game.destiniaeng-106-arm.screenshot3.1332707039326.jpg", "http://api.borqs.com/apk/game.destiniaeng-106-arm.screenshot4.1332707039327.jpg" ],
	    "apk_id" : "game.destiniaeng-106-arm",
	    "download_count" : 1,
	    "install_count" : 3,
	    "uninstall_count" : 0,
	    "favorite_count" : 0*/	   
		var app_name;
		var version_name;
		var file_size;
		var description;
		var icon_url;
		var file_url;
		
		for(var key in o.attachments){
			app_name = o.attachments[key].app_name;
			version_name = o.attachments[key].version_name;
			file_size = o.attachments[key].file_size;
			description = o.attachments[key].description;
			icon_url = o.attachments[key].icon_url;
			file_url = o.attachments[key].file_url;
		}
		file_size=Math.floor(file_size/1048576*1000)/1000+"M";
		description = descriptionshow(description,o.post_id_s);
		var sharetext = new Array(
  			'Share applications : ------',
  			'分享应用：------',
  			'aplicativo de compartilhamento de： ------',
  			'アプリケーションを共有する： ------'
  		);
  		var download = new Array(
  			'Download',
  			'点击下载',
  			'baixar',
  			'ダウンロード'
  		);  
		shareinfo = "<p style='color: silver;'>"+sharetext[locale]+"<br></p><div class='well'>"+"<div style=\"margin-left:0px\" class=\"span1\" ><img  width=48px style=\"border:none\" src=\"" +icon_url +"\"/></div><b>" 
		+app_name+ "<br>version_name:" +version_name+ "<br>size:" +file_size+ "<br></b>"
		+description+ "<br><a href=\"" +file_url+ "\" target=\"blank\">"+download[locale]+"</a>"+"</div>";
	}
	else if (o.type ==64){ //this is link_post
		/*link_post_example
		"url" : "http://www.aqee.net/burnout-is-caused-by-resentment/",
    	"host" : "www.aqee.net",
    	"title" : "是怨恨让你产生了身心疲惫、精疲力尽的感觉",
    	"description" : "昨晚，谷歌的美女副总裁玛丽莎·梅耶尔(Marissa Mayer)在纽约的第92大街上的92Y文化交流中心做了一场演讲。她的演讲中有一部分内容是关于为什么员工会产生身心疲惫、精疲力尽的感觉的。",
    	"many_img_url" : "[\"http://api.borqs.com/links/2798102989956883380_small.jpg\"]",
    	"img_url" : "http://api.borqs.com/links/2798102989956883380_small.jpg"*/
    	var url;
    	var title;
    	var description;
    	var img_url;
    	
    	for(var key in o.attachments){
    		url = o.attachments[key].url;
    		title = o.attachments[key].title;
    		description = o.attachments[key].description;
    		img_url = o.attachments[key].img_url;
    	}
    	description = descriptionshow(description,o.post_id_s);
    	
    	if(img_url.length>0){
    		shareinfo = "<img style=\"width:100px;\" src=\"" +img_url+ "\"><br><a href=\"" +url+ "\" target=\"blank\" >"+title+"</a><br>" +description;	
    	}
    	else if(title.length>0){
    		shareinfo = "<a href=\"" +url+ "\" target=\"blank\">"+title+"</a><br>" +description;
    	}
    	else if(url.length>0){
    		shareinfo = "<a href=\"" +url+ "\" target=\"blank\">"+url+"</a><br>" +description;
    	}
    	else{
    		shareinfo="";
    	}
    	var sharetext = new Array(
  			'Share pages : ------',
  			'分享网页：------',
  			'Compartilhe páginas da web：------',
  			'分かち合いのホームページ ：------'
  		);
    	if(shareinfo.length>0){
    		shareinfo = "<p style='color: silver;'>"+sharetext[locale]+"<br></p><p class='well'>" +shareinfo+ "</p>"
    	}
    	else{
    		shareinfo="";
    	}
		
	}
	else if (o.type ==128){ //this is apk_link_post
		/*apk_link_post_example
		 [{"href":"http://market.android.com/details?id=com.mop.mobile"}]*/
		var href;
		
		for(var key in o.attachments){
			href = o.attachments[key].href;
		}
		var sharetext = new Array(
  			'Share applications : ------',
  			'分享应用：------',
  			'aplicativo de compartilhamento de：------',
  			'アプリケーションを共有する：------'
  		);
		shareinfo = "<p style='color: silver;'>"+sharetext[locale]+"<br></p><p class='well'>"+ "<a href=\"" +href+ "\" target=\"blank\">点击去往android market查看应用信息</a>" +"</p>" ;
	}
	else if (o.type ==256||o.type ==512){ //this is apk_comment_post
		var app_name;
		var version_name;
		var file_size;
		var description;
		var icon_url;
		var file_url;
		
		for(var key in o.attachments){
			app_name = o.attachments[key].app_name;
			version_name = o.attachments[key].version_name;
			file_size = o.attachments[key].file_size;
			description = o.attachments[key].description;
			icon_url = o.attachments[key].icon_url;
			file_url = o.attachments[key].file_url;
		}
		description = descriptionshow(description,o.post_id_s);
		file_size=Math.floor(file_size/1048576*1000)/1000+"M";
   		var sharetext = new Array(
  			'Share applications : ------',
  			'分享应用：------',
  			'aplicativo de compartilhamento de： ------',
  			'アプリケーションを共有する： ------'
  		);
  		var download = new Array(
  			'Download',
  			'点击下载',
  			'baixar',
  			'ダウンロード'
  		); 
		shareinfo = "<p style='color: silver;'>"+sharetext[locale]+"<br></p><div class='well'>"+"<div style=\"margin-left:0px\" class=\"span1\" ><img  width=48px style=\"border:none\" src=\"" +icon_url +"\"/></div><b>" 
		+app_name+ "<br>version_name:" +version_name+ "<br>size:" +file_size+ "<br></b>"
		+description+ "<br><a href=\"" +file_url+ "\" target=\"blank\" >"+download[locale]+"</a>"+"</div>";
	}
	else if(o.type ==4096){ //this is friend_set_post
		/* friend_set_post_example
		{
		    "user_id" : 10369,
		    "display_name" : "Evan",
		    "image_url" : "http://api.borqs.com/sys/icon/1.gif",
		    "address" : [ ],
		    "pedding_requests" : "[]"
	  	}, {
		    "user_id" : 10000,
		    "display_name" : "刘华东™",
		    "image_url" : "http://api.borqs.com/profile_image/profile_10000_1332987613265_M.jpg",
		    "address" : [ {
		      "postal_code" : "",
		      "street" : "北京",
		      "state" : "",
		      "type" : "",
		      "po_box" : "",
		      "extended_address" : "",
		      "city" : "",
		      "country" : ""
	    } ],
	    "pedding_requests" : "[]"
	  },*/
		var display_name;
		var user_id;
		var addfriend = '';
		
		for(var key in o.attachments){
			user_id = o.attachments[key].user_id;
			display_name = o.attachments[key].display_name;
			addfriend += "<a href=\"persionindex.html?persionid=" +user_id+ "\">" +display_name +"</a>"+ "  ";
		}
		var sharetext = new Array(
  			'Add Friend : ------',
  			'添加好友：------',
  			'Adicionar Amigo： ------',
  			'友達を追加： ------'
  		);
  		var add = new Array(
  			'Add friend ',
  			'添加好友 ',
  			'adicionar amigo ',
  			'友人を追加する '
  		);
		shareinfo ="<p>"+add[locale] + addfriend +"</p>" ;
	}

	return shareinfo;
}

function photoclick(object){	
	var attachmentid=object.id.substring(0, object.id.indexOf('i'));
	var imgids=attachmentid+'imgs';
	var imgidl=attachmentid+'imgl';
	$("#"+imgids+"").toggle();
	$("#"+imgidl+"").toggle();
	
}
	var moretext = new Array(
		'more',
		'更多',
		'mais',
		'より多くの'
	);
	var lesstext= new Array(
		'less',
		'收起',
		'menos',
		'以下'
	);
function descriptionshow(s,ids){
	var description = s;
	var id = ids;
	var lessdescrip;
	var moredescrip;
	if(description!=undefined){
		if(description.length > 200){
			var stop=description.indexOf('<br>',200);
			if(stop < 0){
				lessdescri = description.substring(0,200);
				moredescrip = description.substring(200,description.length);
			}
			else{
				lessdescri = description.substring(0,stop);
				moredescrip = description.substring(stop,description.length);
			}
			description = lessdescri+"<a id='"+ids+"m' onclick=showmoreattachment(this)>"+moretext[locale]+">></a>"
				+"<div id='"+ids+"info' style='display:none'>"
				+moredescrip
				+"<a id='"+ids+"l' onclick=hidemoreattachment(this)> <<"+lesstext[locale]+" </a> </div> ";
		}
		else{
			description = description;
		}
	}else{
		description='';
	}

	return description;
}
