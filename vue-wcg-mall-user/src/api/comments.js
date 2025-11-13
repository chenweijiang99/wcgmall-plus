import request from '@/utils/request.js';

export const userAddCommentsService = (comments) => {
    return request.post('/user/comments/addComments',comments);
}


export const userGetCommentsCountService = (fid,module)=>{
    return request.get(`/user/comments/selectCount/${fid}/${module}`);
}

export const userDeleteCommentsService = (id) => {
    return request.delete('/user/comments/deleteComments?id='+id);
}

export const userGetCommentsTreeService = (data)=>{
    const params = new URLSearchParams();
    for (let key in data) {
        params.append(key, data[key]);
    }
    return request({
        url: '/user/comments/selectTree',
        method: 'get',
        params
    })
}
