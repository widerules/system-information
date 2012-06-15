function addreceiver()
{			  
	showFriendList(); 	
	$("#dlgfriends").dialog(
		'option',
		'buttons',
		{
			'Ok':function() {
				$(this).dialog("close");
				//alert("gtmpidlist"+gtmpidlist);
				//alert("gidlist"+gidlist);
				// alert("gtmpnamelist"+gtmpnamelist);
				//alert("gnamelist"+gnamelist);

				//比较tmp与上次,得出应该send出去的name及id
				sendidgroup = [''];
				sendnamegroup = [''];
				//check if have exist,if have,donnot add to the gidlist
				addnum = 0;
				for(var i=0;i<gtmpidlist.length;i++){
					isexist = false;
					for(var j=0;j<gidlist.length;j++){
						if(gidlist[j] == gtmpidlist[i]) {
							isexist = true;				
							break;
						}												
					}
					if(isexist == false) {//新增id,加入到sendidgroup中
						sendidgroup[addnum] = gtmpidlist[i];
						sendnamegroup[addnum] = gtmpnamelist[i];
						addnum=addnum+1;
					}
				}

				for( var i=0; i<sendnamegroup.length; i++) {
					$("#privatenames").val(sendnamegroup[i]);
					var e = jQuery.Event("keypress");//模拟一个键盘事件
					e.which = 44;
					$("#privatenames").trigger(e);//模拟页码框按下回车
				}
				//alert("addnum is:"+addnum);
				for(var k=0;k<addnum;k++) {
					gidlist.push(sendidgroup[k]);
					gnamelist.push(sendnamegroup[k]);
				}
				//alert("gidlist"+gidlist);
				//alert("gnamelist"+gnamelist);
				gtmpidlist.length = 0;
				gtmpnamelist.length = 0;
			},
			'Cancel':function() {
				gidlist.length =0;
				$(this).dialog("close");
			}
		});
	//dialog with modal
	$("#dlgfriends").dialog("open");								
}
