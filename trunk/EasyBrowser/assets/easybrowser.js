function collapse(index) {

	title = document.getElementById("title" + index).firstChild;
	obj = document.getElementById("content" + index);

	collapsed = (obj.style.display === "none");
	window.JSinterface.saveCollapseState(index, !collapsed);

	if (collapsed) {
		obj.style.display = ""; 
		title.nodeValue = "-" + title.nodeValue.substring(1);
	}
	else {
		obj.style.display = "none"; 
		title.nodeValue = "+" + title.nodeValue.substring(1);
	}
}

