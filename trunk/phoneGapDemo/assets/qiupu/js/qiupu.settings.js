image_url = "";

//var tmplocalID = $.cookie('locale');
function ResSettingLocaleString(localeId) {
	//tmplocalID = localeId;
	$.cookie('locale', localeId, { expires: 9999 });//save locale to cookie
	
	$('#home').html(resHome[localeId]);
	$('#info').html(resInfo[localeId]);
	$('#name').html(resName[localeId]);
	$('#sign').html(resSign[localeId]);
	$('#changepwd').html(resChapwd[localeId]);
	$('#gender').html(resSex[localeId]);
	
	$('#cmpname').html(resCmpname[localeId]);
	$('#add').html(resAddr[localeId]);
	
	
	$('#photo').html(resPhoto[localeId]);
	
	document.getElementById("save").value = resSave[localeId];
	document.getElementById("upbutton").value = resUpload[localeId];
	
	var obj = document.getElementById("user_gender");
	obj.options[0].text = resMen[localeId];
	obj.options[1].text = resWm[localeId];
	obj.options[2].text = resSecrete[localeId];	
}
var resHome = new Array(
		'Main Page',
		'主页',
		'página principal',
		'メインページ');
var resInfo = new Array(
		'Personal Information', 
		'个人信息', 
		'Informações Pessoais', 
		'個人情報'
		);
var resName = new Array(
		'Login name', 
		'登录名', 
		'Entrar nome', 
		'ログイン名'
		);
var resSign = new Array(
		'Signature', 
		'个性签名', 
		'assinatura', 
		'署名'
		);
var resChapwd = new Array(
		'Change Password', 
		'修改密码', 
		'alterar senha', 
		'パスワードを変更する'
		);
var resCmpname = new Array(
		'Company Name', 
		'公司名称', 
		'nome da empresa', 
		'会社名'
		);
var resAddr = new Array(
		'Address', 
		'地址', 
		'endereço', 
		'アドレス'
		);
var resPhoto = new Array(
		'Local photos', 
		'本地照片', 
		'fotos locais', 
		'地元の写真'
		);

var resSex = new Array(
		'Gender', 
		'性别', 
		'sexo', 
		'性別'
		);
var resMen = new Array(
		'Male', 
		'男', 
		'masculino', 
		'男性'
		);
var resWm = new Array(
		'Female', 
		'女', 
		'feminino', 
		'女性'
		);
var resSecrete = new Array(
		'Secrecy', 
		'保密', 
		'sigilo', 
		'秘密'
		);
var resSave = new Array(
		'Save', 
		'保存', 
		'salvar', 
		'保存'
		);
var resUpload = new Array(
		'Upload pictures', 
		'上传图片', 
		'upload de fotos', 
		'写真をアップロード'
		);
var resBrower = new Array(
		'Your browser may not support some features, please try another browser or your browser changed to high-speed mode', 
		'您的浏览器有可能不支持某些功能，请尝试其他浏览器或者将浏览器改为高速模式', 
		'O seu browser pode não suportar algumas características, por favor tente outro navegador ou seu navegador alterado para modo de alta velocidade', 
		'お使いのブラウザは、いくつかの機能をサポートしていないかもしれませんが、高速モードに変更し、別のブラウザまたはブラウザをお試しください。');

var resNowpwd = new Array(
		'The current password', 
		'当前密码', 
		'a senha atual', 
		'現在のパスワード'
		);
var resNewpwd = new Array(
		'New password', 
		'新密码', 
		'nova senha', 
		'新しいパスワード'
		);
var resConpwd = new Array(
		'Confirm new password', 
		'确认新密码', 
		'Confirme a nova senha', 
		'新しいパスワードを確認'
		);
var resOK = new Array(
		'Ok', 
		'确定', 
		'determinar', 
		'決定する'
		);


var resNewpwdNot = new Array(
		'Twice to enter a new password is inconsistent', 
		'两次输入新密码不一致', 
		'Duas vezes para inserir uma nova senha é inconsistente', 
		'新しいパスワードを2回入力して矛盾しています'
		);
var resChpwdsuc = new Array(
		'Change password  successfully', 
		'密码修改成功', 
		'Senha alterada com sucesso', 
		'パスワードが正常に変更された'
		);
var reschDispwdfal = new Array(
		'Failed to modify discuz password', 
		'修改discuz密码失败', 
		'Falha ao modificar a senha Discuz', 
		'清華のパスワード変更に失敗しました'
		);
var reschpwdfal = new Array(
		'Change password fail, please try again', 
		'密码修改失败，请重试', 
		'A alteração da senha falhou, por favor tente novamente', 
		'パスワード変更が失敗し、もう一度試してください。'
		);


var resHasave = new Array(
		'Has been saved', 
		'已保存', 
		'foi salvo', 
		'保存されている'
		);
var resChformat = new Array(
		'Please select the image format files', 
		'请选择图片格式文件', 
		'Por favor, selecione os arquivos de formato de imagem', 
		'画像形式のファイルを選択してください。'
		);
var resUpicsuc = new Array(
		'Updated picture success', 
		'更新图片成功', 
		'Sucesso quadro atualizado', 
		'更新された画像の成功'
		);
var resUpicfal = new Array(
		'Upload picture fails', 
		'更新图片失败', 
		'Quadro atualizado falhar', 
		'更新された画像は失敗します'
		);
var resLesspwd = new Array(
		'The password length should not be less than 6', 
		'密码长度不应小于6位', 
		'O comprimento da senha não deve ser inferior a 6', 
		'パスワードの長さが6未満であるべきではありません'
		);
function changepw()
{		
	document.getElementById('settingform').innerHTML = "";
	document.getElementById('imageForm').innerHTML = "";
	$("#settingform").append('<label>'+resNowpwd[locale]+'</label>'	
	+'<input id="oldpassword" class="LengthValidate6 inputText" value="" type="password"/>'
	+'<label>'+resNewpwd[locale]+'</label>'
	+'<input id="newpassword" name="newpassword" class="LengthValidate6 inputText" value="" type="password"/>'
	+'<label>'+resConpwd[locale]+'</label>'
	+'<input id="againnewpassword" name="againnewpassword" class="LengthValidate6 inputText" value="" type="password"/>'
	+'<br><input type="button" class="btn btn-primary" value='+resOK[locale]+' onclick="savepw()"/>');
	
}

//save the pwd 
function savepw()
{
	if($('#newpassword').val() != $('#againnewpassword').val())
	{
		alert(resNewpwdNot[locale]);
		return;	
	}
	
	if($('#newpassword').val().length<6)
	{
		alert(resLesspwd[locale]);
		return;	
	}
	var md5oldpassword =hex_md5($('#oldpassword').val());		
	var md5newpassword =hex_md5($('#newpassword').val());	
	invokeApi("account/change_password", prepareData({"oldPassword":md5oldpassword, "newPassword":md5newpassword}), function(ret){
		if (ret["result"] == true)
		{
			alert(resChpwdsuc[locale]);
			$.cookie('password', md5newpassword,{ expires: 7 });
			
			//修改discuz数据库中的用户密码
			invokeApi("user/show", prepareData({"users":$.cookie('user_id'),"columns":"login_phone1"}),function(ret) {
				if (ret["error_msg"] == undefined )
				{
				   	login_phone = ret[0]["login_phone1"];		
				   	if(login_phone != '')
				   	{
				   		invokeBBSApi("account/change_password", {"login_phone":login_phone,"oldpwd":md5oldpassword,"newpwd":md5newpassword}, function(ret){
							if (ret["result"] == "false")
								alert(reschDispwdfal[locale]);							
							else
								window.location = "settings.html";
						});	  
				   	}				
				}
			});			
		}
		else
			alert(reschpwdfal[locale]);
		
	});
	
	
}


function saveinfo(){
	var usr_name = document.settingForm.login_name.value;	
	var cmp_name = document.settingForm.cmp_name.value;				
	var usr_add = document.settingForm.usr_add.value;
	var user_status = document.settingForm.user_status.value;
	var user_gender = "m";
	if(document.settingForm.user_gender.selectedIndex == 0)//man
		user_gender = "m";
	else if(document.settingForm.user_gender.selectedIndex == 1)//woman
		user_gender = "f";
	else
		user_gender = "u";
	value = [{'postal_code':'','street':usr_add,'state':'','type':'','po_box':'','extended_address':'','city':'','country':''}];
	str = JSON.stringify(value);
	invokeApi("account/update", prepareData({"display_name":usr_name,"company":cmp_name,"address":str,"status":user_status,"gender":user_gender}),function(ret) {
				  if (ret["error_msg"] == undefined )
			      {//if post success						      	
			      		if (ret["result"] == true) {
			      			alert(resHasave[locale]);
			      			window.location="home.html"	
			      		}				      
			      }	
   });				
			   		
}
			
function uploadAndSubmit() {
	invokeApi("user/show", prepareData({"users":$.cookie('user_id'),"columns":"#full"}),function(ret) {
		if (ret["error_msg"] == undefined )
		{
		   if(ret[0]["small_image_url"]!=undefined)
		      image_url=ret[0]["small_image_url"];	  
		}	
    });	
	document.imageForm.appid.value = "1";
	document.imageForm.ticket.value = $.cookie('ticket');
	signstr = getSignData("profile_image");
	document.imageForm.sign.value = signstr;
	var path = document.imageForm.profile_image.value;	
	var index = path.lastIndexOf(".");
	type = path.substring(index+1);
	strtype = type.toLowerCase();
	if((strtype!="jpeg")&&(strtype!="jpg")&&(strtype!="png")&&(strtype!="gif"))
	{
		alert(resChformat[locale]);
		return false;
	}
	//document.imageForm.action = "http://apitest.borqs.com/account/upload_profile_image";
	//document.imageForm.submit();         		
	
	var options ={			        
        type:      'POST',       
        dataType:  'json',      
        url:qiupuip+'account/upload_profile_image',
        beforeSubmit:  showRequest,
        success:callbacksuc,
        error:callbackfail			       
        };
    
	$('#imageForm').ajaxSubmit(options);	 
	return false;
	  		

}  

function showRequest(formData, jqForm, options) {     
    var queryString = $.param(formData); 			 
    //alert('About to submit: \n\n' + queryString); 			   
    return true; 
} 

function callbacksuc(json)
{
	//alert("json is: "+JSON.stringify(json));
	if(json["result"]== true)
		alert(resUpicsuc[locale]);
	else
		alert(resUpicfal[locale]);
	
}

function callbackfail(json)
{
	//比较图片链接，确定是否被替换
	invokeApi("user/show", prepareData({"users":$.cookie('user_id'),"columns":"#full"}),function(ret) {
		if (ret["error_msg"] == undefined ) 
		{
	      	tmp_image_url = "";
	      	if(ret[0]["small_image_url"]!=undefined)
				tmp_image_url=ret[0]["small_image_url"];  
	      			
	  		if(image_url != tmp_image_url)				
	  			alert(resUpicsuc[locale]);
			else
				alert(resUpicfal[locale]);
  
	     }	
    });	
}


/*
var video;

function Opencamera()
{
	//show the photo div
	$('#photo').show();
}
function Takepic()
{	
  video = document.getElementById('video');
  navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia;
  if(navigator.getUserMedia)
  {
    navigator.getUserMedia('video', successCallback, errorCallback);    
    function successCallback(stream)
    {
      if(window.webkitURL)
      {
        console.log('window.webkitURL != undefined');
        video.src = window.webkitURL.createObjectURL(stream);
      }
      else
      {
        console.log('window.webkitURL == undefined');
        video.src = stream;
      }
    }
    function errorCallback(error)
    {
      console.error('An error occurred: [CODE ' + error.code + ']');
      return;
    }        
    
  }
  else
  {
    console.log('You are not worthy.');
  }
  
}

function Savepic()
{	  
	var img = new Image();	 
	img.src = "a.jpg";	
	var imgdata="";
	
	img.onload = function(){  	 
		var canvas =document.getElementById('canvas');  		  
		var ctx = canvas.getContext('2d');  
		ctx.drawImage(img,0,0);	
		imgdata = canvas.toDataURL('image/jpeg');
		//alert(imgdata);
		
		index = imgdata.indexOf(",");
		imgdata1 = imgdata.substr(index+1);
			 
		signstr = getSignData("profile_image");
		alert(signstr);   
		   
		   
		data = {'appid':"1","ticket":$.cookie('ticket'),"sign":signstr,"profile_image":imgdata1};
		$.ajax({
		 url:qiupuip+'account/upload_profile_image',     
		 data: data,
		 dataType:"json",
		 type: 'POST',
		 success: function () {
		     alert("success");
		 },          
		 error:function () {
		     alert("error");
		 },
		 complete:function(){
		 	alert("complete");
		 }
		     
		});  
	 	
	}
	   
} */
