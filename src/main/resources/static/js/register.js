$(document).ready(function() {
    // 이메일 인증 버튼 클릭 이벤트
    $('#emailVerifyBtn').on('click', function() {
        const email = $('#email').val();

        if (!email) {
            alert('이메일을 입력하세요.');
            return;
        }

        // 이메일 인증 API 호출
        $.ajax({
            url: 'http://web.dokalab.site:8084/api/redis',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ key: email, value: verificationCode }),
            success: function(response) {
                console.log('Success:', response);
                alert('인증 코드가 이메일로 전송되었습니다.');
                $('#emailVerifyBtn').addClass('verified').text('인증 완료');
                $('#emailVerifyBtn').prop('disabled', true);
            },
            error: function(error) {
                console.log('Error:', error);
                alert('인증 코드 전송 중 오류가 발생했습니다. 다시 시도해주세요.');
            }
        });
    });

    // 비밀번호 확인 로직
    $('#password, #passwordConfirm').on('keyup', function() {
        const password = $('#password').val();
        const passwordConfirm = $('#passwordConfirm').val();

        if (password !== passwordConfirm) {
            $('#password-match-message').show();
            $('#submit-btn').prop('disabled', true);
        } else {
            $('#password-match-message').hide();
            $('#submit-btn').prop('disabled', false);
        }
    });
});