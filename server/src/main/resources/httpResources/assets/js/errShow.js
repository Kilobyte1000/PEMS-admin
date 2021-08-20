let params = new URLSearchParams(location.search)
if (params.get('err') != null) {
	document.getElementById('main').style.alignSelf='baseline'

	errCode = params.get('err')
	p = document.getElementById('errPara');

	if (errCode == 1) { // Provided Data does not match with data in database
		p.innerText = "The provided data does not match. Please try again" 

	} else if(errCode == 2) { //Provided Admission no. is not found in the database
		p.innerText = "The provided Admission no. was not found"

	} else if(errCode == 3) { // Users has already voted
		p.innerText = "The provided Admission no. has already voted"

	} else { // Warn to stop them playing around with my webpage
		p.innerText = "You should not be fiddling around with this webpage"
	}
}