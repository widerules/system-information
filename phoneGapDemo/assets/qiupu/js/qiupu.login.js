
function setLocale(localeId) {
	if (localeId === $.cookie('locale')) return;//just return if locale not changed

	locale = localeId;//global variable

	setLocaleString(localeId);
}

function setLocaleString(localeId) {
	$.cookie('locale', localeId, { expires: 9999 });//save locale to cookie
	$('#aBrand').html(resBrand[localeId]);
	$('#aContact').html(resContact[localeId]);
	$('#aContactdiscuz').html(resContact[localeId]);
	$('#aBox').html(resBox[localeId]);
	$('#labelEmail').html(resAccount[localeId]);
	$('#labelPass').html(resPassword[localeId]);
	$('#aReset').html(resReset[localeId]);
	$('#inputLogin').html(resLogin[localeId]);
	$('#inputRegister').html(resRegister[localeId]);
	$('#inputGuest').html(resGuest[localeId]);	
	$('#question').html(resVerify[localeId] + ': '  + answer);
	$('#spanEmail').html(resEmail[localeId]);
	$('#spanPass').html(resPass[localeId]);
	$('#spanAnswer').html(resAnswer[localeId]);
}

var resBrand = new Array(
		'Phoenix3', 
		'梧桐', 
		'Phoenix3', 
		'インダス');

var resContact = new Array(
		'Contact us', 
		'联系我们', 
		'contacte-nos', 
		'お問い合わせ');

var resBox = new Array(
		'App Store', 
		'应用宝盒', 
		'App Store', 
		'App Storeで');

var resAccount = new Array(
		'Email or ID', 
		'手机号、邮箱或BorqsID', 
		'E-mail ou ID', 
		'電子メールまたはID');

var resPassword = new Array(
		'Password&nbsp&nbsp&nbsp&nbsp', 
		'密码&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp', 
		'Senha&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp', 
		'パスワード&nbsp&nbsp');

var resReset = new Array(
		'Fogot your password?', 
		'重置密码', 
		'Redefinir a senha', 
		'パスワードをリセットする');

var resVerify = new Array(
		'Please input verify code', 
		'请输入验证字符', 
		'Por favor verifique código de entrada', 
		'入力を確認するコードをしてください');

var resLogin = new Array(
		'Login', 
		'登录', 
		'Login', 
		'ログイン');

var resRegister = new Array(
		'Register', 
		'注册', 
		'Registrar', 
		'登録');
		
var resGuest = new Array(
		'Anonymous', 
		'随便逛逛', 
		'anônimo', 
		'匿名の');		

var resEmail = new Array(
		'Please input account', 
		'请输入帐号', 
		'Por favor conta de entrada', 
		'入力アカウントをしてください');

var resPass = new Array(
		'Please input password', 
		'请输入密码', 
		'Por favor senha de entrada', 
		'入力のパスワードをしてください');

var resAnswer = new Array(
		'Verify error, please input again', 
		'验证错误，请重新输入', 
		'Verifique erro, por favor coloque novamente', 
		'エラーを確認し、もう一度入力してください');
