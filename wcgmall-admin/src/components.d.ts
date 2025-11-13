declare module 'vue' {
  export interface GlobalComponents {
    ElButton: typeof import('element-plus')['ElButton']
    ElForm: typeof import('element-plus')['ElForm']
    ElFormItem: typeof import('element-plus')['ElFormItem']
    ElInput: typeof import('element-plus')['ElInput']
    ElContainer: typeof import('element-plus')['ElContainer']
    ElHeader: typeof import('element-plus')['ElHeader']
    ElAside: typeof import('element-plus')['ElAside']
    ElMain: typeof import('element-plus')['ElMain']
    ElMenu: typeof import('element-plus')['ElMenu']
    ElMenuItem: typeof import('element-plus')['ElMenuItem']
    ElIcon: typeof import('element-plus')['ElIcon']
    ElDropdown: typeof import('element-plus')['ElDropdown']
    ElDropdownMenu: typeof import('element-plus')['ElDropdownMenu']
    ElDropdownItem: typeof import('element-plus')['ElDropdownItem']
    ElCard: typeof import('element-plus')['ElCard']
    ElRow: typeof import('element-plus')['ElRow']
    ElCol: typeof import('element-plus')['ElCol']
  }
}

export {} 