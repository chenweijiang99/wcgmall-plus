import { ref } from "vue";
import { defineStore } from 'pinia';
import { loginApi, getUserInfoApi, logoutApi } from "@/api/auth";
import { store } from "@/stores";
import { setToken, removeToken } from "@/utils/auth";
import { User } from "@/types";

export const useUserStore = defineStore("user", () => {
  const user = ref<User>();
  /**
   * 登录
   *
   * @param {LoginData}
   * @returns
   */
  function login(loginData: any) {
    return new Promise<void>((resolve, reject) => {
      loginApi(loginData)
        .then((response) => {
          const { data } = response;
          setToken(data.token)
          resolve();
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  // 获取信息
  function getUserInfo() {
    return new Promise<any>((resolve, reject) => {
      getUserInfoApi()
        .then(({ data }) => {
          if (!data) {
            reject("Verification failed, please Login again.");
            return;
          }
          user.value = data;
          resolve(data);
        })
        .catch((error) => {
          reject(error);
        });
    });
  }

  // user logout
  function logout() {
    return new Promise<void>((resolve, reject) => {
      logoutApi()
        .then(() => {
          removeToken()
          user.value = undefined;
          location.href = '/login';
          resolve();
        })
        .catch((error) => {
          removeToken()
          location.href = '/login';
          resolve();
        });
    });
  }

  // remove token
  function resetToken() {
    return new Promise<void>((resolve) => {
      removeToken()
      resolve();
    });
  }

  return {
    user,
    login,
    getUserInfo,
    logout,
    resetToken,
  };
});

// 非setup
export function useUserStoreHook() {
  return useUserStore(store);
}
