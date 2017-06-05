package com.lbbento.pitchupwear.main

import com.lbbento.pitchuptuner.GuitarTunerReactive
import com.lbbento.pitchuptuner.service.TunerResult
import com.lbbento.pitchupwear.common.StubAppScheduler
import com.lbbento.pitchupwear.util.PermissionHandler
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import rx.Observable.just

class MainPresenterTest {

    val mockPermissionHandler: PermissionHandler = mock()
    val mockView: MainView = mock()
    val mockGuitarTunerReactive: GuitarTunerReactive = mock()
    val mockMapper: TunerServiceMapper = mock()
    val stubAppSchedulers = StubAppScheduler()
    val mainPresenter = MainPresenter(stubAppSchedulers, mockPermissionHandler, mockGuitarTunerReactive, mockMapper)

    @Before
    fun setup() {
        mainPresenter.onAttachedToWindow(mockView)
    }

    @Test
    fun shouldSetAmbienteEnabledOnCreate() {
        mainPresenter.onCreated()

        verify(mockView).setAmbientEnabled()
    }

    @Test
    fun shouldDoNothingWhenNoAudioPermissions() {

        //Given
        whenever(mockPermissionHandler.handleMicrophonePermission()).thenReturn(false)

        //On
        mainPresenter.onViewResuming()

        //Should handle permissions
        verify(mockPermissionHandler).handleMicrophonePermission()

        //Should do nothing
        verify(mockGuitarTunerReactive, never()).listenToNotes()
    }

    @Test
    fun shouldUpdateTunerViewWhenReceivedFromService() {
        val tunerResult: TunerResult = mock()
        val tunerViewModel: TunerViewModel = mock()

        //Given
        whenever(mockGuitarTunerReactive.listenToNotes()).thenReturn(just(tunerResult))
        whenever(mockPermissionHandler.handleMicrophonePermission()).thenReturn(true)
        whenever(mockMapper.tunerResultToViewModel(tunerResult)).thenReturn(tunerViewModel)

        //On
        mainPresenter.onViewResuming()

        //should handle permissions
        verify(mockPermissionHandler).handleMicrophonePermission()

        //should start tuner service
        verify(mockGuitarTunerReactive).listenToNotes()
        verify(mockView).updateTunerView(tunerViewModel)
    }
}
