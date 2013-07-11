function hideTop() {
	document.getElementById('title1').style.display = 'none';
	document.getElementById('content1').style.display = 'none';
}

function collapse(para) {
	var tmp = String(para).split(',');
	var index = tmp[0];
	var title = document.getElementById('title' + index).firstChild;
	var content = document.getElementById('content' + index);
	var collapsed = (content.style.display === 'none');
	if (tmp.length == 1) window.JSinterface.saveCollapseState(index, !collapsed);
	else collapsed = (tmp[1] == 'true')?true:false;
	if (collapsed) { 
		content.style.display = ''; 
		title.nodeValue = '-' + title.nodeValue.substring(1); 
	} else { 
		content.style.display = 'none'; 
		title.nodeValue = '+' + title.nodeValue.substring(1);
	}
}

function inject(data) {
	if (data.indexOf('::::') == 0) return;

	var tmp = data.split('::::');
	var array = tmp[1].split('....');
	var title = document.getElementById('title' + tmp[0]);
	var content = document.getElementById('content' + tmp[0]);
	var arrayLength = array.length - 1;// the last element is noise
	for (i = 0; i < arrayLength; i++) {
		if (content.children.length <= i) {
			var li=document.createElement('li');
			content.appendChild(li);
			li.outerHTML = array[i];// must set value after append, otherwise got NO_MODIFICATION_ALLOWED_ERR
		} else if (content.children[i].outerHTML != array[i]) content.children[i].outerHTML = array[i]; 
	}
	while (content.children.length > arrayLength) {
		var child = content.children[arrayLength];
		content.removeChild(child);
	}
	if (arrayLength == 0) title.style.display = 'none';
	else title.style.display = '';
}

function setTitle(para) {
	document.title = para;
}

function setTitleBar(para) {
	var data = String(para).split(',');
	var title = document.getElementById('title' + data[0]);
	var content = document.getElementById('content' + data[0]);
	collapsed = (data[1] == 'true')?true:false;
	if (collapsed) {
		title.innerHTML = '+\t' + data[2];
		content.style.display = 'none';
	} else {
		title.innerHTML = '-\t' + data[2];
		content.style.display = '';
	}
}

function setButton(para) {
	var data = String(para).split(',');
	document.getElementById('edit_home').innerHTML = data[0];
	document.getElementById('delete_selected').innerHTML = data[1];
	document.getElementById('cancel_edit').innerHTML = data[2];
}

function showCheckbox() {
	document.getElementById('edit_home').style.display='none';
	document.getElementById('delete_selected').style.display='';
	document.getElementById('cancel_edit').style.display='';
	var inputs = document.getElementsByTagName('input');
	for (var i = 0; i < inputs.length; i++) inputs[i].style.display='';
}

function hideCheckbox() {
	document.getElementById('edit_home').style.display='';
	document.getElementById('delete_selected').style.display='none';
	document.getElementById('cancel_edit').style.display='none';
	var inputs = document.getElementsByTagName('input');
	for (var i = 1; i < inputs.length; i++) inputs[i].style.display='none';
}

function deleteSelected() {
	var bookmarks = '';
	var historys = '';
	var inputs = document.getElementsByTagName('input');
	for (var i = 0; i < inputs.length; i++) {
		if (inputs[i].checked) {
			if (inputs[i].attributes['class'].nodeValue == 'bookmark') bookmarks += i + ',,,,';
			else historys += i + ',,,,';
		}
	}
	window.JSinterface.deleteItems(bookmarks, historys);
	hideCheckbox();
}
