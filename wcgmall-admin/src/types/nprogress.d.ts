declare module 'nprogress' {
  interface NProgressStatic {
    start(): void
    done(): void
    configure(options: { showSpinner: boolean }): void
  }
  
  const nprogress: NProgressStatic
  export default nprogress
} 