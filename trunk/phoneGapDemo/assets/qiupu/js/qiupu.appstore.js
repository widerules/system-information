function onAppDetail(app_package) {
	$.cookie('app_package', app_package, { expires: 1 });
	window.location = 'appDetail.html';
}

function appendApp(id, appDetail) {
	$(id).html('');//clear the component at first
	$.each(appDetail, function(i, item) {
		tmp = '<tr style=\'border:1px #f0f0f0 solid\'>'
		tmp += 		'<td><div class=appTitle><img onclick=javascript:onAppDetail(\'' + item.package + '\') src=' + item.icon_url + ' onload=resizeimg(this,40,40)><h3><a class=hideOnPhone href=javascript:onAppDetail(\'' + item.package + '\')>' + item.app_name + '</a></h3></div></td>';
		tmp += 		'<td><div class=appDescription>'
		tmp += 			'<h4><a href=javascript:onAppDetail(\'' + item.package + '\')>v' + item.version_name + '</a></h4>';
		tmp += 			item.description.substr(0, 50) + '...';
		tmp += 		'</div></td>'
		tmp += 		'<td><div class=\'btn btn-large btn-primary appDownloadBtn\' onclick=javascript:window.location=\'http://api.borqs.com/search?q=' + item.package + '\';>' + resDownload[locale] + '</div></td>';
		tmp += '</tr>';
		$(id).append(tmp);
	});
}

//show the app list
function getappstoreAll(pageidx,needfreshpage) {		
	gpageidx = pageidx;
	fun = 'getappstoreAll';
	gpnexttimes = parseInt(pageidx/gpagenumshow);	
	/*
	url = 'qiupu/app/all';
	data = prepareData({
		'page' : gpnexttimes,
		'count':gpagenumshow*10,			
		'cols':'app_name'
	});*/

		
	
	//get apps of certain page
	invokeApi("qiupu/app/all", {'page':pageidx, 'count':'10', 'cols':'package, app_name, version_name, description, icon_url'}, function(ret) {
		if(ret=='')
			alert(resLastPage[locale]);
		else{
			appendApp('#applist', ret);
			showpage(pageidx,fun,gpnexttimes);
		}
	});
}


function setLocaleString(localeId) {
	$('#appBrand').html(resAppBrand[localeId]);
	$('#mainPage').html(resMainPage[localeId]);
}

var resAppBrand = new Array(
		'Apps',
		'应用宝盒',
		'aplicação Pack',
		'アプリケーションパック');
