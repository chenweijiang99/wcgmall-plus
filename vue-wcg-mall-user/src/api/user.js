import request from '@/utils/request.js'

export const userRegisterService = (data) => {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

export const userLoginService = (loginData) => {
    return request({
        url: '/auth/login',
        method: 'post',
        loginData
    })
}
export const getUserInfoService = () => {
    return request({
        url: "/user/user/userInfo",
        method: "get",
    })
}

export const logoutService = () => {
    return request.post('/user/user/logout');
}

export const userActiveService = (activeData) => {
    const params = new URLSearchParams();
    for (let key in activeData) {
        params.append(key, activeData[key]);
    }
    return request.post('/user/user/activate', params);
}

export const userCheckCode = (checkData) => {
    const params = new URLSearchParams();
    for (let key in checkData) {
        params.append(key, checkData[key]);
    }
    return request.post('/user/user/activateCode', params);
}

export const userGetCode = (email) => {
     return request({
    url: '/auth/getEmailCode',
    method: 'get',
    params: {
      email
    }
  })
}

export const userUpdateService = (data) => {
    return request({
        url: '/user/user/update',
        method: 'put',
        data,
    })
}

export const userEditePasswordService = (passwordData) => {
    const params = new URLSearchParams();
    for (let key in passwordData) {
        params.append(key, passwordData[key]);
    }
    return request.post('/user/user/editPassword', params);
}

export const userUploadAvatarService = (avatarData) => {
    return request.post('/user/user/upload', avatarData);
}

export const userGetCodeByRestPwdService = (email) => {
    return request.get('/user/user/getCodeByResetPwd?email=' + email);
}
export const userActivateCodeByRestPwdService = (checkData) => {
    const params = new URLSearchParams();
    for (let key in checkData) {
        params.append(key, checkData[key]);
    }
    return request.post('/user/user/activateCodeByRestPwd', params);
}
export const userResetPasswordService = (checkData) => {
    const params = new URLSearchParams();
    for (let key in checkData) {
        params.append(key, checkData[key]);
    }
    return request.post('/user/user/resetPassword', params);
}

export const userGetJuHeAuthService = (type) => {
    return request.get(`/user/user/juhe/getJuHeAuth/${type}`);
}