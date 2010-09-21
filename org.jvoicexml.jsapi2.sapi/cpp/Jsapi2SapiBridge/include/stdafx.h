#pragma once

#include "targetver.h"
#define WIN32_LEAN_AND_MEAN

// Windows-Headerfiles:
#include <sapi.h>
#include <sphelper.h>
#include "resource.h"


#include <iostream>
void GetErrorMessage(char* buffer, size_t size, const char* text, HRESULT hr);

#define WM_RECOEVENT    WM_APP+2

extern HWND hWnd;
