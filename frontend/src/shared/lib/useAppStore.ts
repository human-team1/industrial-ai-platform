import { createContext, useContext } from 'react';

type AppState = {
  appName: string;
  setAppName: (appName: string) => void;
};

// 1. 초기값 설정
const defaultState: AppState = {
  appName: 'Industrial AI Platform',
  setAppName: () => {},
};

// 2. Context 생성
export const AppContext = createContext<AppState>(defaultState);

// 3. 기존 useAppStore 이름을 유지하여 다른 파일의 수정을 최소화
export const useAppStore = () => useContext(AppContext);