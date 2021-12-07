$.validator.setDefaults({
    onkeyup: false,
    onclick: false,
    onfocusout: false,
    showErrors: function(errorMap,errorList){
        if(this.numberOfInvalids()){ // 에러가 있으면
            alert(errorList[0].message); // 경고창으로 띄움
            errorList[0].element.focus();
        }
    }
});

$.validator.addMethod('passRegex', function(value,element,regexpr) {
    return regexpr.test(value)
}, '비밀번호 형식이 일치하지 않습니다.');
