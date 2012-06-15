function setLocaleString(localeId) {
	$('#mainPage').html(resMainPage[localeId]);
	$('#btnComments').html(resAddComments[localeId]);
}

var resRating = new Array(
		'Rating: ',
		'评分：',
		'Classificação: ',
		'評価: ');

var resDownloads = new Array(
		'Downloads: ',
		'下载次数：',
		'Download: ',
		'ダウンロード: ');

var resSize = new Array(
		'Size: ',
		'应用大小：',
		'Tamanho: ',
		'サイズ: ');
