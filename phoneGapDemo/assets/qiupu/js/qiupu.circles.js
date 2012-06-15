
/* Global Variables*/
var gcircleId = 0;  //id of current circle being operated
var gcircleNum = 0; //number of peoples in current circle
var gidlist = new Array();
var gnamelist = new Array();
var gtmpidlist = new Array(); //list of id of selected friends
var gtmpnamelist = new Array();
var gmemberidlist = new Array();
var gccidlist = new Array();           //list of id of selected circles
var gcirname = '';
var gsearchname = '';         //search name 
var friendmenber=new Array();// all friend's id'
var gjsonfriend = null;
var gjsoncircle = null;
var gtouch=0;

  		
//find the locale name for given circle                                      
function getcirclelocale(orgname,idx)
{
	var mapname = orgname;
	idx++;
	for(var i=0; i<rescircleNames[0].length; i++) {
		if(rescircleNames[0][i] == orgname) {
		   mapname = rescircleNames[idx][i];
		   break;			
		}
	}
	return mapname;
}


function initModalDialogs() {
	
	// select users for adding
    $("#dlgfriends").dialog({
    	                   modal:true,
    	                   bgiframe: true,
    	                   height: 320,
    	                   autoOpen:false, 
    	                   title:resChooseFriend[locale]                    	                 
    	                 });
    	                 
     //filter data from the friend list via keyword	                 
    $("#filterkey").keyup(function(){
                           changeFriendList($('#filterkey').val());
                         })
                    .blur(function(){
                           changeFriendList($('#filterkey').val());
                         });	    	                 
                    
	                                            	                   	                 
    // select users for adding
    $("#dlgcircles").dialog({
    	                   modal:true,
    	                   bgiframe: true,
    	                   autoOpen:false, 
    	                   title:resChooseCircle[locale],	                    	                 
    	                 });    	                 
    	                
    // create new circle  
    $("#dlgeditname").dialog({
    	                   modal:true,
    	                   bgiframe: true,    	                   
    	                   autoOpen:false, 
    	                   title:resCircleCreate[locale],
    	                   buttons:{
    	                   	          'OK':function()
    	                              {
    	                             	createcircle($('#newcirclename').val());
    	                             	$(this).dialog("close");	                 	             
    	                              },
    	                 	           
                                      'Cancel':function()
    	                              {
    	                             	$(this).dialog("close");
  	                 	              }   	                 	     
    	                 	       }  	                    	                 
    	                 });
}


	 

function getUserInfo(json)
{        
	$("#listmystat").html('');                 	                          	                        	                              	  
    $.each
    (   json,
        function(i,o) 
        {
        		image_href = "javascript:settings("+o.user_id+")";
        		$('#index-name').html(o.display_name);
        		$('.index-setting').attr('href',image_href);
                $("#listmystat").append(
					                    "<h4>"
                                        + o.display_name
                                        + "</h4>"
                                        + "<hr><br>" 
					                    + "<div>"					    						                                                         
                                        + "<a href="+image_href+">" 
                                        + "<img class='hideOnPhone' src=" + o.image_url + " onload=resizeimg(this,96,96)></a>"
                                        + "<p>"				                        
   				                        + "<a href=\"javascript:friendshow(0);\">"+ resFriendtitle[locale]+"("+o.friends_count+")</a>"			      	
				                        + "|"
				                        + "<a href=\"javascript:followershow(0)\">"+ resFollowertitle[locale]+"("+o.followers_count+")</a>"		      	
                                	    + "</div>"
                                	    ); 
        }
    );                                  	  	                                        	                            	  
}


function showUserInfo(id)
{	
	invokeApi("user/show", {'users':id, 'columns':'#full'}, getUserInfo);
}

function AddUserToCircle(userid, circleid)
{	
   var lstuserid = '';
   for(var i=0;i<userid.length;i++)
     lstuserid = lstuserid + userid[i] + ',';
     
   if(('' != circleid) && ('' != lstuserid)) {
    	invokeApi("friend/usersset",
    	          prepareData({'circleId':circleid, 'friendIds':lstuserid.substr(0,lstuserid.length - 1)}), 
    	          afterEditCircle
    	         );   	
   }

}


function AddUserAsFriend(circlelist,usrid)
{
   var lstccid = '';
   for(var i=0;i<circlelist.length;i++) {
     if(circlelist[i] !== '') lstccid = lstccid + circlelist[i] + ',';   	
   }

   if(('' !== lstccid) && ('' !== usrid))
    	invokeApi("friend/circlesset",
    	          prepareData({'friendId':usrid, 'circleIds':lstccid.substr(0,lstccid.length - 1)}), 
    	          afterEditCircle
    	         );		   

}

function AddAllToList(myitem)
{
	 var checked = myitem.checked;
     $("#selectfriends .selectable").each(function () {
           var subChecked = $(this).attr("checked");
           if (subChecked != checked) {
               $(this).attr("checked",checked);
               AddToList(this);           	
           }
     });  
}

function AddToList(myitem)
{	
	if(myitem.checked == true) //记录选中id
	{		
		gtmpidlist.push(myitem.id);
		gtmpnamelist.push(myitem.name);		
	}
	else	
	{
		for(var i=0;i<gtmpidlist.length;i++)
		{
			if(gtmpidlist[i] == myitem.id)
			{
				gtmpidlist.splice(i,1);
				gtmpnamelist.splice(i,1);
			}
				
		}		
	}
}

function AddToCircleList(myitem)
{	
    var varid = myitem.id.substring(0, myitem.id.indexOf('selcircle'));		
	if(myitem.checked==true) 
	{
	   gccidlist.push(varid);	
	}
	else
	{
		for(var i=0;i<gccidlist.length;i++)
		{
			if(gccidlist[i] == varid)
			{
				gccidlist.splice(i,1);
			}			
		}			
	}    	
}

//根据查询字更新
function changeFriendList(keyword)
{
	var strchk = '';
	$("#selectfriends").html('');
	$.each(gjsonfriend,
		   function(i,o)
	       { 	       	
	       	   if(o.display_name.indexOf(keyword) >= 0)
	       	   {
	       		  strchk = "";  
	       		  for(var i=0;i<gtmpidlist.length;i++)
	       		  {
	       			  if(gtmpidlist[i] == o.user_id)
	       			  {
	       				strchk = " checked "
	       				break;
	       			  }
	       		  }
	       		             
	              $("#selectfriends").append("<div style='float:left;width:150px'>"  
	             	                    + "<input class='selectable' type=\"checkbox\" onclick=\"javascript:AddToList(this)\""
	             	                    + " name="+ o.display_name
	             	                    + " id="+ o.user_id + strchk + ">"
	                                    + "<img src=" + o.image_url + " onload=resizeimg(this,36,36)>"
	                                    + o.display_name
	                                    + "</tr>");	       	   	
	       	   }
	       	   
	       	   	                                    		
          }
    ); 
      
} 


//显示用户的好友列表
function showFriends(json)
{
	gjsonfriend = json;
	$("#selectfriends").html('');
	$.each(json,
		   function(i,o)
	       { 	 
	       	   friendmenber[i]=o.user_id;                        	       	     	  
	           $("#selectfriends").append("<div style='float:left;width:150px'>"  
	             	                    + "<input class='selectable' type=\"checkbox\" onclick=\"javascript:AddToList(this)\"" 
	             	                    + " name="+ o.display_name
	             	                    + " id="  + o.user_id + ">"
	                                    + "<img src=" + o.image_url + " onload=resizeimg(this,36,36)>"
	                                    + o.display_name
	                                    + "</div>");		
          }
    );  
    
    //commented by liuchengtao  
    //refreshed(gtabsel, gpageidx);    
} 

//默认显示所有的好友
function showFriendList() 
{         
	$('#filterkey').val(''); 
	
	$('#btnallornone').attr("checked",false);

    invokeApi("friend/show",prepareData({'page':0, 'count':9999}), showFriends);    
}

//从当前圈子删除好友
function removecirclesset(id)
{	
	var usrid = id.substring(0, id.indexOf('requestrmfriend'));
	
	delFriendById(usrid,gcircleId);	
}

//调整好友到其他圈子
function updatecirclesset(id)
{
	//用户的id
	var usrid = id.substring(0, id.indexOf('requestmodfriend'));
	 
    //清空列表	
	gccidlist.length=0;	
	   
	//初始化对话框的返回事件
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
    	                    });
    	                    
    getUserInCircles(usrid);	                    
    	              	              	
}

function AddMemberToList(myitem)
{	
	if(myitem.checked == true) //记录选中id
	{		
		gmemberidlist.push(myitem.id);	
	}
	else	
	{
		for(var i=0;i<gmemberidlist.length;i++)
		{
			if(gmemberidlist[i] == myitem.id)
			{
				gmemberidlist.splice(i,1);
			}				
		}		
	}
}


function AddMembersToList(myitem)
{
	 var checked = myitem.checked;
     $("#timelines .selectable").each(function () {
           var subChecked = $(this).attr("checked");
           if (subChecked != checked) {
               $(this).attr("checked",checked);
               AddMemberToList(this);         	
           }
     });  
}


function delSelectedFriends(ccid) {
	var memids = '';
	for(var i=0;i<gmemberidlist.length;i++) {	
		memids = memids + ',' + gmemberidlist[i].substring(0, gmemberidlist[i].indexOf('uid'));
	}	
	
	if(memids !== '') {
	    delFriendById(memids,ccid);		
	}
}


//显示圈子内的成员 
function circlepeoples(json)
{	
	beforeload();  
	document.getElementById('timelines').innerHTML = "";
	document.getElementById("pageshow").innerHTML = "";
	
	var addMemberstext = new Array(
		'Add members',
		'添加好友',
		'Adicionar membros',
		'メンバーを追加する'
	);
	
	var delMemberstext = new Array(
		'Remover selecionados',
		'移除选择',
		'remover membros',
		'選択し削除します'
	);	
	
	var searchsigntext = new Array(
		'already in circles:',
		'已加入圈子：',
		'já em círculos:',
		'すでにサークルで:'
	);	
		                   
	gmemberidlist.length = 0;
	                   
	$("#timelines").append("<div class='row'>" 	  
						   +"<div class='hideOnPhone span2'>"
	                       + "<section class='pull-left' id='checkmembersall'>"
					       + "<input type='checkbox' onclick='AddMembersToList(this)'>"
					       + resCheckAllorNot[locale]	
					       + "</section>"						   					    
					       +"</div>"	  
					       +"<div class='span4' style='margin-left:15px;'>"	
	                       + "<section class='pull-right btn btnprimary' onclick=\"delSelectedFriends('"+ gcircleId+"')\">-" 
	                       + delMemberstext[locale]
	                       + "</section>"					       				                         	
	                       + "<section class='pull-right btn btnprimary' onclick=\"addFriendsDialog('"+ gcircleId+"')\">+" 
	                       + addMemberstext[locale]
	                       + "</section>"	                       		
					       + "</div>"	                       		                             
	                       + "</div><br><br>");
		
    $.each(json,	
	       function(i,jsont) {	       	       		       		
	       		if(jsont["member_count"] != undefined) {
	       	        gcircleNum=jsont["member_count"] ;
	       	    }
	       		var oo=jsont["members"];	       		      		
				$.each(oo, 											
					   function(j, o) {
						var status='';					    
					    $("#timelines").append("<div class='row' id='"+o.user_id+"member'>" 
						    +"<div class='hideOnPhone span1'> "						    
						    +"<a class='hideOnPhone'>"
					        +"<img src=" + o.image_url +" onload=resizeimg(this,60,60)>"
					        +"</a>"
					        +"</div>"
					        +"<div class='span5' style='margin-left:15px;'>"
					        + "<input type='checkbox' class='selectable' id='"+o.user_id+"uid' onclick='AddMemberToList(this)' >"			        
					        + getdisplayname(o)	
					        + removefromcircle(o)					        
					        + adjustcircle(o)						        				        					        					        				        
					        + "</div>"
						    + "</div><hr>");
				});	
		   });
    afterload();  	                                                                               						           			                    
}

//给定的圈子的详细信息
function showcirclebyid(id)
{     			                      	                          	                        	                              	  
    invokeApi("circle/show",prepareData({'circles':id, 'with_users':true}), circlepeoples);                               	  	                                        	                            	  
}	

//所有圈子基本信息
function showcircleall()
{
    invokeApi("circle/show",prepareData({'circles':'', 'with_users':false}), circlesummary); 
}

function showCircleNameList(defcircles,callback)
{
	
    $('#newcirclenamesel').val('');	
			         	 	   
    invokeApi("circle/show",
              prepareData({'circles':'', 'with_users':false}),     	      	
	          function(data){
	          	  document.getElementById('selectcircles').innerHTML = "";
	              $.each(data,
		          function(i,o)
	              { 	    	                    	        	                    	
	       	         //地址簿 & 黑名单     
                    if('address book' == o.circle_name.toLowerCase()){}
                    else if('blocked' == o.circle_name.toLowerCase()){}                         
                    else
                    {
                     $("#selectcircles").append("<tr>"  
              	                             + "<td>"
             	                             + "<input type=\"checkbox\""
             	                             + " id='" + o.circle_id +"selcircle'"
             	                             + " onclick=\"javascript:AddToCircleList(this)\">"
                                             + "</td>"               
                                             + "<td>"
                                             + getcirclelocale(o.circle_name,$.cookie('locale'))
             	                             + "</td>"
                                             + "</tr>");                                                                                                             	
                   } 	       		
                });
                                         
                var idgroup = defcircles.split("|");
		        for(var i=0;i<idgroup.length;i++)
                {
                    gccidlist.push(idgroup[i]);
                    $('#'+idgroup[i]+'selcircle').attr('checked',true);	
                } 
                
                if(callback !== null && callback !== undefined) 
                    callback();
        }); 
                                          
}


function showBtnAddCircle()
{
	if($("#lstAddCircle").is(":hidden"))
	  $("#lstAddCircle").show()
	else
	  $("#lstAddCircle").hide(); 	  
}

function createCircleDialog()
{
	gcirname = '';		
	$("#dlgeditname").dialog("open");		
}

function createCircleforSel()
{
	gcirname = '';		
	createcircle($('#newcirclenamesel').val());		
}

function createcircle(circlename)
{      
	gcirname = circlename;
	
	if(gcirname != '') {
       invokeApi("circle/create",prepareData({'name':gcirname}), afterOpCircle);  		
	}                   	                          	                        	                              	  
                  	  	                                        	                            	  
}	

function delcirclebyid(id)
{ 
	if(confirm(resDeleteCircle[locale]))
	{     
	   gcircleId = id;                     	                          	                        	                              	  
       invokeApi("circle/destroy",prepareData({'circles':id}), afterOpCircle);  
    }                  	  	                                        	                            	  
}	


function delFriendById(userid,cirid)
{	
	if(confirm(resDeleteFriend[locale]))
	{ 
        invokeApi("friend/usersset",prepareData({'circleId':cirid, 'friendIds':userid,'isadd':false}), afterEditCircle);     		
	}	
}

function addFriendsDialog(circleid)
{	 
	gcircleId = circleid;	
	gidlist.length =0;
	gtmpidlist.length =0;	
	showFriendList();	 		
	$("#dlgfriends").dialog( 'option',
	                         'buttons',
	                         {
	                      	   'OK':function()
    	                        {
    	                            AddUserToCircle(gtmpidlist,gcircleId);
    	                            $(this).dialog("close");	                 	             
    	                        },
    	                 	           
                                'Cancel':function()
    	                        {
    	                            $(this).dialog("close");
  	                 	        }   	                 	     
    	                     } 
    	                ); 
	
	$("#dlgfriends").dialog("open");			        	
}


function showcircleposts(id,num)
{
	document.getElementById("pageshow").innerHTML = "";
    if(id.indexOf('circleid')>=0) 
    { 
       $('.circleitem').removeClass('active');
       $('#'+id).addClass('active');     	
       gcircleId = id.substring(id.indexOf('circleid') + 8,id.length);      	
    }  	
    
    if(num != -1) {
       gcircleNum = num;
    } 
      
    circletimeline(0);      	
}

function showcirclepeoples(id)
{	
    if(id.indexOf('circleid')>=0) {
       $('.circleitem').removeClass('active');
       $('#'+id).addClass('active');     	
       gcircleId = id.substring(id.indexOf('circleid') + 8,id.length);       	
    }
    
	gtabsel = 'circlepeople';	
	$('#usercontent').hide();
	$('#nfxcontent').hide();	
	$('#maincontent').show();	
				
	$('#tabs').hide();	
	$('#tabcircles').show();		
	$('#circlepeople').addClass('active');
	$('#circlepost').removeClass(); 
	  	
    showcirclebyid(gcircleId);  
}


function circlesummary(json)
{   		
    if (json["error_msg"] == undefined )
	{ 
	    document.getElementById('listcircle').innerHTML = "";	 
	  
	    $("#listcircle").append("<br><h4>"
	                         + resCircletitle[locale]
	                         + "<a id='linkaddcircle' style=\"display:none;float:right;\" href=\"javascript:createCircleDialog()\">"
	                         + "<small>+"+resCircleCreate[locale]+"</small></a>"
	                         + "</h4><hr><br>"
						     + "<ul id='tbcircle' class='nav nav-list nav-stacked'></ul>");
						     									     			
        $("#listcircle").bind('mouseover',
                             function()
                             {                    	
                    	         $("#linkaddcircle").show();   
                    	     })                  	               
                    	.bind('mouseleave',
                              function()
                              {                    	
                    	          $("#linkaddcircle").hide();    
                    	      });						     												     	
	 
	    gjsoncircle = json;	 	 	                                                                       	                 	                          	                        	                              	      
        $.each(json,
              function(i,o) 
              {     
          	   //地址簿 保留   //黑名单 保留       
               if('address book' == o.circle_name.toLowerCase()){}             
               else if('blocked' == o.circle_name.toLowerCase()){}
               else
               {          
               	    var circlename = getcirclelocale(o.circle_name,$.cookie('locale'));
               	    
	                $("#tbcircle").append("<li id=\"circleli" + o.circle_id +"\" >"
                                          + "<section class='circleitem' id=\"circleid" + o.circle_id +"\" onclick=\"showcircleposts(this.id,"+o.member_count+");\" >"
                                          + "<img src='style/images/circle_icon.png' style='vertical-align:bottom;border-style:none;padding-right:2px;'>"                                    
                                          + circlename
                                          + "<span id=\"circlecnt" + o.circle_id +"\">"
                                          + "</span>"                                                                                                                          
                                          + "<a style=\"display:none;float:right;\" id=\"circleop"+ o.circle_id + "\""
                                          + " href=\"javascript:delcirclebyid('" + o.circle_id + "')\">"
                                          + "[x]</a>"
                                          +"</section>"                                                                                                                                                               
                                          + "</li>"                                                                                                                                                                                                     
                                         ); 
                                                                                               
                    //custom defined circle can be removed                            
                    if(circlename === o.circle_name) 
                    {                   	
                         $("#circleli" + o.circle_id).bind('mouseover',
                                                           function()
                                                           {                    	
                    	                                       $("#circleop" + o.circle_id).show();   
                    	                                   })
                    	                             .bind('mouseleave',
                                                           function()
                                                           {                    	
                    	                                       $("#circleop" + o.circle_id).hide();    
                    	                                   });             	                                       			                   
                    }  
                                 	
               	    $("#circlecnt" + o.circle_id).html("("+ o.member_count+")");             	                  	         	
              }                            
          }
        );
     
        $('#circleid'+ gcircleId ).addClass('active');
   }
                                      	  	                                        	                            	  
} 	

function getUserInCircles(id){

	invokeApi("user/show",
	          prepareData({'users':id, 'columns':'in_circles'}), 
              function(json){  
	              	                   var incircles='';              	                	      	
	                          $.each(json, 
	                          	     function(i, o) {	              	
	              	                   var oo = o.in_circles;

	              	                   $.each(oo, 
	              	 	                      function(j, vv){
	              	 	                      incircles = incircles + '|' + vv["circle_id"];	              	 	
	              	                   });
           	 
	                         });
	              	 
	              	          showCircleNameList(incircles,
	              	 	                         function() {
	              	                                $("#dlgcircles").dialog('open'); 	              	 	
	              	          });	  	                                                            
	         });
}

function settings(id)
{
	if(id == $.cookie('user_id'))				
		window.location = "settings.html";	
}

/*event for search users*/
function onbtnSearch()			
{
    searchusershow($('#usersearchtxt').val());	    
}

function onKeySearch(event)			
{
	if (event.keyCode == 13)
	{
        searchusershow($('#usersearchtxt').val());	
	}   		
}
	




