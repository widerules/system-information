var intervalid1 = 0;
var intervalid2 = 0;
var pushgetcodetimes = 0;//用户点击获取验证码次数
var remainTime = 0;//剩余时间
var regbyphone = false;

//var tmplocalID = $.cookie('locale');
function ResRegLocaleString(localeId) {
	//tmplocalID = localeId;
	$.cookie('locale', localeId, { expires: 9999 });//save locale to cookie
	
	$('#lareemail').html(resReEmail[localeId]);
	$('#larephone').html(resRePhone[localeId]);
	
	
	$('#laemail').html(resRegEmail[localeId]);
	$('#laphone').html(resPhone[localeId]);
	$('#lapwd').html(resPwd[localeId]);
	$('#note').html(resNote[localeId]);
	$('#ladev').html(resDev[localeId]);
	$('#lasex').html(resSex[localeId]);
	$('#lacode').html(resCode[localeId]);		

	document.getElementById("btnGetveriCode").value = resGetCode[localeId];
	document.getElementById("btnSubmitCode").value = resSubmit[localeId];
	document.getElementById("regbyemail").value = resSubmit[localeId];
	
	
	var obj = document.getElementById("gender");
	obj.options[0].text = resMen[localeId];
	obj.options[1].text = resWm[localeId];
	obj.options[2].text = resSecrete[localeId];	
	
}

function ResetLocaleString(localeId) {
	//tmplocalID = localeId;
	$.cookie('locale', localeId, { expires: 9999 });//save locale to cookie	
	
	$('#labemailre').html(reSetbyEmail[localeId]);
	$('#labphonere').html(reSetbyPhone[localeId]);
	
	
	$('#labemail').html(reSetEmail[localeId]);
	$('#labphone').html(reSetPhone[localeId]);	
	$('#inputcode').html(reSetcode[localeId]);
	
		

	document.getElementById("btnGetveriCode").value = resGetCode[localeId];
	document.getElementById("btnSubmitCode").value = reSetpwd[localeId];
	document.getElementById("btnResEmail").value = reSetpwd[localeId];	
	
	
}

function ResbindLocaleString(localeId) {
	//tmplocalID = localeId;
	$.cookie('locale', localeId, { expires: 9999 });//save locale to cookie	
	
	
	$('#labphone').html(resInputphone[localeId]);	
	$('#labcode').html(reSetcode[localeId]);	
	document.getElementById("btnGetveriCode").value = resGetCode[localeId];
	document.getElementById("btnSubmitCode").value = resBind[localeId];
		
	
	
}
var resBind = new Array(
		'Bind', 
		'绑定', 
		'igação', 
		'結合');

var reSetbyEmail = new Array(
		'Use email to reset', 
		'邮箱重置', 
		'E-mail para redefinir', 
		'リセットするには、電子メール');

var reSetbyPhone = new Array(
		'Use phone to reset', 
		'手机重置', 
		'Repor telefone', 
		'電話機のリセット');

var reSetPhone = new Array(
		'Please enter your registered phone', 
		'请输入您注册时使用的手机号', 
		'Por favor, indique o número de telefone quando você se cadastra', 
		'あなたが登録時に電話番号を入力してください。');
var reSetEmail = new Array(
		'Please enter your registered mailbox', 
		'请输入您注册时使用的邮箱', 
		'Digite seu correio registado', 
		'ご登録のメールボックスを入力してください。');
var reSetcode = new Array(
		'fill out the verification code', 
		'填写验证码', 
		'preencher o código de verificação', 
		'確認コードを記入');

var reSetpwd = new Array(
		'Reset the password', 
		'重置密码', 
		'Redefinir a senha', 
		'パスワードをリセットする');



var resReEmail = new Array(
		'Use email to sign up', 
		'邮箱注册', 
		'E-mail de registro', 
		'E-メール登録');
var resRePhone = new Array(
		'Use phone to sign up', 
		'手机号注册', 
		'Número de telefone registrado', 
		'電話番号の登録');


var resRegEmail = new Array(
		'Email', 
		'邮箱', 
		'E-mail', 
		'E-メール');

var resPhone = new Array(
		'Phone', 
		'手机号', 
		'número de telefone', 
		'電話番号');
var resPwd = new Array(
		'Password', 
		'密码', 
		'Senha', 
		'パスワード'
		);
var resNote = new Array(
		'Password length should not less than six', 
		'为了确保您的账户安全，请增强密码强度并妥善保管。长度不应小于6位', 
		'A fim de garantir a sua conta de segurança, força de senha avançado e guarda. Comprimento não inferior a seis', 
		'あなたのアカウントのセキュリティ、強化されたパスワードの強度と保管を確実にするためである。 6に満たない長さ'
		);
var resDev = new Array(
		'Nickname', 
		'昵称', 
		'apelido', 
		'ニックネーム'
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

var resCode = new Array(
		'Phone verification code', 
		'手机验证码', 
		'Código de verificação Handset', 
		'携帯電話の検証コード'
		);
var resSubmit = new Array(
		'Sign Up', 
		'立即开通', 
		'registro', 
		'登録'
		);
var resGetCode = new Array(
		'Get phone verification code for free', 
		'免费获取手机验证码', 
		'Acesso livre ao código de confirmação por telefone', 
		'電話確認コードへの無料アクセス'
		);


var resNotbind = new Array(
		'Your account is not bound to the phone. Click OK to bind', 
		'您的帐号没有绑定手机号，不能登录论坛。点击确定绑定手机', 
		'Sua conta não está vinculada ao número de telefone celular não pode entrar no fórum. Clique em OK para ligar telefone', 
		'アカウントが携帯電話の番号にバインドされていないフォーラムにログオンすることはできません。電話をバインドするために[OK]をクリックします'
		);

var resCoderror = new Array(
		'Verification code error or timeout', 
		'验证码错误或超时', 
		'Código de verificação de erro ou timeout', 
		'検証コードエラーまたはタイムアウト'
		);
var resHasReset = new Array(
		'Password has been reset, will sent to your phone as message', 
		'密码已重置，将以短信形式发送到您的手机', 
		'Senha foi redefinida, será a mensagem enviada para o seu telefone', 
		'パスワードがリセットされており、お使いの携帯電話に送信されるメッセージになります'
		);
var resRetror = new Array(
		'Reset the password fails, please try again', 
		'重置密码失败，请重试', 
		'Redefinir a senha falhar, por favor tente novamente', 
		'パスワードが失敗し、リセット、再試行してください。'
		);
var resLink = new Array(
		'Binding link send to your phone, please click', 
		'绑定链接以短信方式发送到您的手机，请点击', 
		'Elo para enviar uma mensagem de texto para o seu telefone, por favor clique', 
		'お使いの携帯電話にテキストメッセージを送信するために結合リンクは、クリックしてください。'
		);
var resInputphone = new Array(
		'Please enter the phone number', 
		'请输入手机号', 
		'Por favor, indique o número de telefone', 
		'電話番号を入力してください。'
		);
var resRightphone = new Array(
		'Please enter the correct phone number', 
		'请输入正确的手机号', 
		'Por favor, indique o número de telefone correto', 
		'正しい電話番号を入力してください。'
		);

var resInputpwd = new Array(
		'Please enter the password', 
		'请输入密码', 
		'Digite uma senha', 
		'パスワードを入力してください'
		);
var resInputdev = new Array(
		'Please enter the nickname', 
		'请输入昵称', 
		'Digite um apelido', 
		'ニックネームを入力してください。'
		);
var resInputcode = new Array(
		'Please enter the verification code', 
		'请输入验证码', 
		'Por favor insira o código de verificação', 
		'確認コードを入力してください。'
		);

var resreget = new Array(
		'To reacquire', 
		'重新获取', 
		'para readquirir', 
		'再取得するために'
		);
var resMins = new Array(
		'Minutes after', 
		'分钟后', 
		'minutos depois', 
		'分後'
		);
var resSecs = new Array(
		'seconds after', 
		'秒后', 
		'segundos depois', 
		'秒後'
		);
var resSendcoderror = new Array(
		'Failed to send the verification code', 
		'发送验证码失败', 
		'Falha ao enviar o código de verificação', 
		'確認コードの送信に失敗しました'
		);

var resReccode = new Array(
		'The verify code has been sent, please check', 
		'验证码已发送，请注意查收', 
		'Verifique se o código foi enviada, por favor, note que o check', 
		'コードが送信されていることを確認し、チェックすることに注意してください'
		);
var resFre = new Array(
		'Too frequent request, please try again later.', 
		'请求过于频繁，请稍后再试', 
		'Solicitação muito freqüente, por favor, tente novamente mais tarde.', 
		'あまりに頻繁なリクエストは、後でもう一度試してみてください。'
		);

var resHasReg = new Array(
		'This number has been registered, will be transferred to the login', 
		'此号码已经注册过,现在将转入登录界面', 
		'Este número foi registada, será transferida para a tela de autenticação', 
		'この番号が登録されていると、ログイン画面に転送されます。'
		);
var resEmailHasReg = new Array(
		'This mailbox is already registered', 
		'此邮箱已经注册过', 
		'Esta caixa de correio já está registrado', 
		'このメールボックスは既に登録されています'
		);

var resRetLog = new Array(
		'Back to Login', 
		'返回登录', 
		'Voltar para o Login', 
		'ログインするためにバックアップ'
		);
var resInvEmail = new Array(
		'valid mailbox please', 
		'请输入合法邮箱', 
		'Por favor, indique uma caixa postal válida', 
		'有効なメールボックスを入力してください。'
		);
var resLesspwd = new Array(
		'The password length should not be less than 6', 
		'密码长度不应小于6位', 
		'O comprimento da senha não deve ser inferior a 6', 
		'パスワードの長さが6未満であるべきではありません'
		);
var resRsMailSuc = new Array(
		'Password has been reset, will be mailed to your mailbox', 
		'密码已重置，将以邮件形式发送到您的邮箱', 
		'A senha foi redefinida, será enviado para sua caixa postal', 
		'パスワードがリセットされている、あなたのメールボックスに郵送されます'
		);
var resRegSuc = new Array(
		'The activate message has been sent to your mail box, please check it to active your account.',
		'注册成功，激活邮件已经发送至注册邮箱，请前往邮箱检查激活帐户.',
		'A inscrição é bem sucedido, ativar a mensagem tenha sido enviada para seu e-mail, por favor, marque para ativar sua conta.', 
		'登録が成功すると、メッセージはメールに送信されたアクティブにすると、アカウントのアクティベーションを確認するためにメールボックスにアクセスしてください.'
		);
//检验号码合法性
function check_phone(str){
	if(str=="") return false;

	if(str.length<11) return false;

	var phoneReg=/^1[3|4|5|8][0-9]\d{4,8}$/;
	if(!phoneReg.test(str)) 
		return false; 
    return true;
}

//进入论坛
function enterDiscuz(){                         
    //获取注册的手机号      
    var login_phone = '';   
    invokeApi("user/show", prepareData({"users":$.cookie('user_id'),"columns":"display_name,login_phone1"}),function(ret) {
            if (ret["error_msg"] == undefined )
            {
                login_phone = ret[0]["login_phone1"];
                nick_name = ret[0]["display_name"];                                                
                if(login_phone!=''){                                            
                        data = {"password": $.cookie('password'),"login_phone":login_phone, "uid": $.cookie('user_id')};
                        invokeBBSApi("account/create", data, function(ret) {
                        //if success,enter into discuz
                        if (ret["result"] == "false")
                        {
                                alert("write data to discuz error!");
                                return false;
                        }
                        window.location="http://bbs.borqs.com/logging.php?action=login&loginsubmit=yes&username="+login_phone+"&password="+$.cookie('password')+"&nickname="+encodeURI(nick_name);
                    });
                }
				else {
					var ret = confirm(resNotbind[locale]);
					if (ret == true) window.location="bind_phone.html";
				}
           }       
    });
}


function call(ret)
{
	if (ret["error_msg"] != undefined ) alert(resCoderror[locale]);//
	else {//return {result:true} if register ok, we just use this account to login. 
		//create the user
		var md5password =hex_md5($('#password').val());
		invokeApi("account/create", {"display_name":$('#devName').val(), "gender":$('#gender').val(), "password": md5password, "login_phone":$('#phone').val()}, function(ret){
			if (ret["error_msg"] != undefined ) {
                alert(ret["error_msg"]);			
			}
			else
			{
				//create account to discuz
				invokeApi("user/show", prepareData({"users":$.cookie('user_id'),"columns":"display_name,login_phone1"}),function(ret) {
	                if (ret["error_msg"] == undefined )
	                {
	                        var login_phone = ret[0]["login_phone1"];
	                        var nick_name = ret[0]["display_name"];   
	                        if (nick_name == '') 
	                        {
	                        	alert("the nickname is null");
	                        	return false;   
	                        }                                          
	                        if(login_phone!=''){                                            
                                data = {"password": $.cookie('password'),"login_phone":login_phone, "uid": $.cookie('user_id'), "nickname":encodeURI(nick_name)};
                                invokeBBSApi("account/create", data, function(ret) {
	                                //if success,enter into discuz
	                                if (ret["result"] == "false")
	                                {
	                                        alert("write data to discuz error!");
	                                        return false;
	                                }
                                });
	                        }
	                }
	             });
				//login
				invokeApi("account/login", {"login_name": $('#phone').val(), "password": md5password}, function(ret){
					if (ret["error_msg"] != undefined ) {
                        $('#password').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
                        $('#password').next().text(resPwderror[locale]).show();					
					}
					else
					{
						$.cookie('user_id', ret["user_id"],{ expires: 7 });
						$.cookie('ticket', ret["ticket"],{ expires: 7 });
						$.cookie('display_name', ret["display_name"],{ expires: 7 });
						$.cookie('login_name', ret["login_name"],{ expires: 7 });
						$.cookie('password', md5password,{ expires: 7 });
						if(ForDiscuz == false)						
							window.location = "home.html";
						else
							enterDiscuz();
					}
				});
				
			}
		});
		
	}	
		
}

//reset pwd by phone
function callreset_phone(ret)
{
	if (ret["error_msg"] != undefined ) alert(resCoderror[locale]);
	else {
		//http://apitest.borqs.com/account/reset_password_for_phone?phone=xxxx
		invokeApi("account/reset_password_for_phone", {"phone": $('#phone').val()}, function(ret){
			if(ret["error_msg"] == undefined)
			{
				alert(resHasReset[locale]);
				window.location = "login.html";	
			}
			else
				alert(resRetror[locale]);
			
		});
		
	}	
		
}

//邮箱注册用户，绑定手机号
function callbindphone(ret)
{
	if (ret["error_msg"] != undefined ) alert(resCoderror[locale]);
	else {		
		invokeApi("account/bind", prepareData({"phone":$('#phone').val()}), function call(ret){
			if (ret["error_msg"] != undefined ) {
                alert(ret["error_msg"]);			
			}
			else
			{
				alert(resLink[locale]);
				window.location = "home.html";
					
			}
		});
	}
}

//注册立即开通
function submitCode() {
	
	if($('#phone').val() == ""){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resInputphone[locale]).show();
		return false;
	}
	else
	{
		var vl = check_phone($('#phone').val());
		if(vl == false)
		{
			$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
			$('#phone').next().text(resRightphone[locale]).show();
			return false;
		}		
	}	

	if($('#password').val() == ""){
		$('#password').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#password').next().text(resInputpwd[locale]).show();
		return false;
	}
	else{
		if($('#password').val().length <6){
			$('#password').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
			$('#password').next().text(resLesspwd[locale]).show();
			return false;
		}
	}
	if($('#devName').val() == ""){
		$('#devName').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#devName').next().text(resInputdev[locale]).show();
		return false;
	}
		
	if($('#code').val() == ""){
        $('#code').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
        $('#code').next().text(resInputcode[locale]).show();
        return false;    
    }			
   
	invokeApi("verify/phone", {"phone": $('#phone').val(),"code":$('#code').val()}, call);		
        
}

function resetbyphone() {
	//alert("type is:"+type);
	if($('#phone').val() == ""){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resInputphone[locale]).show();
		return false;
	}
	else
	{
		var vl = check_phone($('#phone').val());
		if(vl == false)
		{
			$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
			$('#phone').next().text(resRightphone[locale]).show();
			return false;
		}		
	}		
	if($('#code').val() == ""){
        $('#code').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
        $('#code').next().text(resInputcode[locale]).show();
        return false;    
    }			
   
	//重置密码和绑定手机也需调用此api，用参数区分	
	invokeApi("verify/phone", {"phone": $('#phone').val(),"code":$('#code').val()}, callreset_phone);
	
		
		
        
}

function bindphone() {
	//alert("type is:"+type);
	if($('#phone').val() == ""){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resInputphone[locale]).show();
		return false;
	}
	else
	{
		var vl = check_phone($('#phone').val());
		if(vl == false)
		{
			$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
			$('#phone').next().text(resRightphone[locale]).show();
			return false;
		}		
	}		
		
	if($('#code').val() == ""){
        $('#code').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
        $('#code').next().text(resInputcode[locale]).show();
        return false;    
    }			
   
	//重置密码和绑定手机也需调用此api，用参数区分
	invokeApi("verify/phone", {"phone": $('#phone').val(),"code":$('#code').val()}, callbindphone);
		
		
        
}

//更新剩余时间显示
function update() {
	if(remainTime == 0) //说明循环结束
	{
		document.getElementById("btnGetveriCode").value = resreget[locale]; //按钮文字
		document.getElementById("btnGetveriCode").disabled=false; //按钮可点
		window.clearInterval(intervalid1); 
		window.clearInterval(intervalid2); 
	}
	else
	{
		remainTime = remainTime-1;
		if(remainTime>180)
			document.getElementById("btnGetveriCode").value='('+parseInt(remainTime/60)+resMins[locale]+')'+resreget[locale];
		else
			document.getElementById("btnGetveriCode").value='('+remainTime+resSecs[locale]+')'+resreget[locale];		
		
	}
}	


//send the request and show the next span
function send_request(ret){
	if (ret["error_msg"] != undefined ) {
		alert(resSendcoderror[locale]);
		document.getElementById("btnGetveriCode").disabled=false;
	}
	else
	{								
		alert(resReccode[locale]);
		//get the next_span	and start the timer
		invokeApi("verify/phone", {"phone": $('#phone').val(),"next_span":true}, function(ret){
			if (ret["error_msg"] == undefined )
			{											
				remainTime = parseInt(ret["result"]) ;
				//alert("remainTime is"+remainTime);
				if(remainTime >0)
					intervalid1 = setInterval("update()",1000); //设置的时间乘以1000毫秒即调用方法次数
				else
					document.getElementById("btnGetveriCode").disabled=false; 

			}										
		});
	}
					
}


//to check the next span
function get_nextspan(ret){
	if (ret["error_msg"] == undefined )
	{
		remainTime = parseInt(ret["result"]) ;
		if(remainTime >0)//set the timer
		{
			alert(resFre[locale]);
			intervalid1 = setInterval("update()",1000); //设置的时间乘以1000毫秒即调用方法次数
		}
		//call the api to send verify code
		else 
			invokeApi("verify/phone", {"phone": $('#phone').val()}, send_request);
		
	}
}

//check whether has register
function check(ret)
{
	if (ret["result"] > 0) {
		alert(resHasReg[locale]);
		document.getElementById("btnGetveriCode").disabled=false; //re-enable to click
		window.location = "login.html";
	}
	else
	{	
		//to check the next span
		invokeApi("verify/phone", {"phone": $('#phone').val(),"next_span":true}, get_nextspan);				
	}	
	
}

//click the button to get the verify code
function getCode()
{	
	if($('#phone').val() == ""){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resInputphone[locale]).show();
		return false;
	}
	
	var vl = check_phone($('#phone').val());
	if(vl == false)
	{
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resRightphone[locale]).show();
		return false;
	}	
	
	else {
		document.getElementById("btnGetveriCode").disabled=true; //disable the button. it's better to add a gif
		//call the api to check whether the account has registered
		invokeApi("account/who", {"login": $('#phone').val()}, check);
	}
				
}


//click the button to get the verify code for reset pwd
function resetpwd_getCode()
{	
	if($('#phone').val() == ""){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resInputphone[locale]).show();
		return false;
	}
	
	var vl = check_phone($('#phone').val());
	if(vl == false)
	{
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resRightphone[locale]).show();
		return false;
	}	
	
	else
	{	
		document.getElementById("btnGetveriCode").disabled=true; //不能点击
		//to check the next span
		invokeApi("verify/phone", {"phone": $('#phone').val(),"next_span":true}, get_nextspan);				
	}	
				
}


//click the button to get the verify code for bind the phone
function bindphone_getCode()
{	
	if($('#phone').val() == ""){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resInputphone[locale]).show();
		return false;
	}
	
	var vl = check_phone($('#phone').val());
	if(vl == false)
	{
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#phone').next().text(resRightphone[locale]).show();
		return false;
	}	
	
	else
	{	
		document.getElementById("btnGetveriCode").disabled=true; //不能点击
		//to check the next span
		invokeApi("verify/phone", {"phone": $('#phone').val(),"next_span":true}, get_nextspan);				
	}	
				
}



var md5password = '';
var email = '';
/*检测合法邮箱格式*/
function check_email(str){
var emailReg=/^([a-zA-Z0-9_\-\.\+]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\]?)$/;
    if(str=="") return false;
    if(!emailReg.test(str))
   		return false;    
    return true;
}

//check the email
function emailregcall(ret)
{
	if (ret["error_msg"] != undefined )
	{
		if(ret["error_msg"].indexOf("The login name is existing") >-1)
			alert(resEmailHasReg[locale]);
		else
			alert(ret["error_msg"]);			
	}		
	else {//return {result:true} if register ok, we just use this account to login. but why always login fail?
		document.getElementById('register').innerHTML = "";
		$('#register').append(resRegSuc[locale]);				//window.open("mail."+email.substring((email.indexOf('@'))+1,email.length));
	    $("#register").append("<p><a href='login.html'>"+resRetLog[locale]+"</a>");
	}
}

//
function regbyemail()
{	   
	if($('#devName').val() == ""){
		$('#devName').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#devName').next().text(resInputdev[locale]).show();
		return false;
	}
	
	if(!check_email($('#phone').val())){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});				
		$('#phone').next().text(resInvEmail[locale]).show();
		return false;
	}

	if($('#password').val() == ""){
		$('#password').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#password').next().text(resInputpwd[locale]).show();
		return false;
	}
				
	if($('#password').val().length <6){
		$('#password').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#password').next().text(resLesspwd[locale]).show();
		return false;
	}

	/*if($('#pwdagain').val() == ""){
		$('#pwdagain').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#pwdagain').next().text("请确认密码").show();
		return false;
	}
	
	if($('#pwdagain').val() != $('#password').val()){
		$('#pwdagain').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});
		$('#pwdagain').next().text("密码不一致").show();
		return false;
	}*/

	password = $('#password').val();
	md5password = hex_md5(password);
	invokeApi("account/create", {"display_name":$('#devName').val(), "gender":$('#gender').val(), "password": md5password, "login_email":$('#phone').val()}, emailregcall);
}

//reset pwd by email
function ResetByEmail()
{	
	if(!check_email($('#phone').val())){
		$('#phone').css({"background":"#FFAFAF"},{"border":"1px solid #ccc"});				
		$('#phone').next().text(resInvEmail[locale]).show();
		return false;
	}
	//http://apitest.borqs.com/account/reset_password_for_phone?phone=xxxx
	invokeApi("account/reset_password", {"login_name": $('#phone').val()}, function(ret){
		if(ret["error_msg"] == undefined)
		{
			alert(resRsMailSuc[locale]);
			window.location = "login.html";	
		}
		else
			alert(resRetror[locale]);
		
	});		
}

