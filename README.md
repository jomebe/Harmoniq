# Harmoniq

Harmoniq는 YouTube Data API 기반 음악 검색과 Android Media3 로컬 음악 재생을 제공하는 앱입니다. YouTube 검색 결과는 공식 YouTube 앱 또는 웹에서 열며, 내 기기 음악은 앱에서 재생할 수 있습니다.

## 주요 기능

- YouTube 음악·아티스트 검색 및 인기 음악 탐색
- 공식/Topic 채널 결과를 우선 확인할 수 있는 아티스트 검색
- MediaStore 기반 내 기기 음악 검색 및 재생
- ExoPlayer 기반 MP3 스트리밍
- MediaSessionService 백그라운드 재생
- 알림·잠금화면 재생/일시정지/이전/다음 컨트롤
- 재생 목록의 다음 곡 자동 재생
- 최근 청취 아티스트·장르 기반 기기 내 추천
- 저장한 곡과 최근 재생 기록을 Room에 보관
- 화면 꺼짐, 앱 전환, 오디오 출력 변경 처리

## 실행

Android Studio에서 프로젝트를 열고 Gradle Sync 후 실행합니다. 별도의 API 키나 OAuth 설정은 필요하지 않습니다.

빌드 명령: ./gradlew testDebugUnitTest lintDebug assembleDebug

## 구조

- data: YouTube Data API, MediaStore, Room, 저장소
- domain: 트랙/큐 모델과 로컬 추천 엔진
- player: Media3 MediaSessionService와 앱 컨트롤러
- ui: Compose 화면, 컴포넌트, 테마

## 데이터와 라이선스

YouTube 검색 결과는 공식 YouTube 앱 또는 웹에서 재생됩니다. Harmoniq는 YouTube 동영상·음원을 다운로드, 추출하거나 백그라운드 재생을 우회하지 않으며, 로컬 음악 재생 기록만 사용자 기기에 저장합니다.
