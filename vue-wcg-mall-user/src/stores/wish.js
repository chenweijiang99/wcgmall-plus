import {defineStore} from 'pinia'
import {ref} from 'vue'
const useWishListStore = defineStore('wishList',()=>{
    const wishList =ref({})

    const setWishList = (newWshList) =>{
        wishList.value = newWshList
    }

    const removeWishList = ()=>{
        wishList.value = {}
    }

    return {
        wishList,
        setWishList,
        removeWishList
    }
},{persist:true})

export default useWishListStore