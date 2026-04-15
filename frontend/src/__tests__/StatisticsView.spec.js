import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { nextTick } from 'vue'

// Track every chart instance created by echarts.init so we can assert on them.
let chartInstances

vi.mock('echarts', () => ({
  init: vi.fn((el) => {
    const chart = { setOption: vi.fn(), resize: vi.fn(), dispose: vi.fn() }
    chartInstances.push(chart)
    return chart
  }),
}))

// Mock API calls — path is relative to the test file (src/__tests__/).
vi.mock('../api/study', () => ({
  getStudyStatistics: vi.fn(() =>
    Promise.resolve({
      code: 200,
      data: {
        totalStudied: 100,
        totalCorrect: 80,
        totalAccuracyRate: 0.8,
        rangeDurationSeconds: 3600,
        streakDays: 5,
        totalPoints: 200,
        maxStreakDays: 10,
        pendingReviewCount: 3,
        totalWrongCount: 20,
        rangeStudyCount: 50,
        rangeCorrectCount: 40,
        rangeAccuracyRate: 0.8,
        startDate: '2026-04-01',
        endDate: '2026-04-07',
        trend: [
          { date: '2026-04-01', studyCount: 10, correctCount: 8, accuracyRate: 0.8, durationSeconds: 600, pointsEarned: 20 },
          { date: '2026-04-02', studyCount: 15, correctCount: 12, accuracyRate: 0.8, durationSeconds: 900, pointsEarned: 30 },
        ],
      },
    }),
  ),
  getStudyFamiliarityDistribution: vi.fn(() =>
    Promise.resolve({
      code: 200,
      data: [
        { level: 0, count: 5 },
        { level: 1, count: 10 },
        { level: 2, count: 20 },
        { level: 3, count: 15 },
        { level: 4, count: 8 },
        { level: 5, count: 2 },
      ],
    }),
  ),
  getCheckinCalendar: vi.fn(() =>
    Promise.resolve({
      code: 200,
      data: {
        currentStreakDays: 3,
        days: [],
      },
    }),
  ),
}))

// Stub Element Plus components so slot content (including v-if/v-else
// chart containers) renders correctly.
const globalStubs = {
  'el-radio-group': { template: '<div><slot /></div>', props: ['modelValue'] },
  'el-radio-button': { template: '<button><slot /></button>', props: ['label'] },
  'el-alert': { template: '<div />', props: ['title', 'type', 'closable', 'showIcon'] },
  'el-card': {
    template: '<div class="el-card-stub"><slot name="header" /><slot /></div>',
    props: ['shadow'],
  },
  'el-empty': {
    template: '<div class="el-empty-stub"><slot /></div>',
    props: ['description'],
  },
  'el-button': { template: '<button><slot /></button>', props: ['type'] },
}

/**
 * Flush all pending promises and micro-tasks many times so that the full
 * async pipeline completes:  API resolve → state update → DOM re-render →
 * nextTick → renderChart (async, awaits dynamic import) → echarts.init.
 */
const settleAsyncWork = async () => {
  for (let i = 0; i < 15; i++) {
    await flushPromises()
    await nextTick()
  }
}

describe('StatisticsView — handleResize', () => {
  let wrapper
  let addEventListenerSpy
  let removeEventListenerSpy

  beforeEach(() => {
    chartInstances = []
    addEventListenerSpy = vi.spyOn(window, 'addEventListener')
    removeEventListenerSpy = vi.spyOn(window, 'removeEventListener')
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
      wrapper = null
    }
    addEventListenerSpy.mockRestore()
    removeEventListenerSpy.mockRestore()
  })

  const mountComponent = async () => {
    const { default: StatisticsView } = await import(
      '../views/statistics/StatisticsView.vue'
    )
    wrapper = mount(StatisticsView, {
      global: {
        stubs: globalStubs,
        directives: { loading: () => {} },
      },
      attachTo: document.body,
    })
    await settleAsyncWork()
  }

  it('should register a resize listener on window when mounted', async () => {
    await mountComponent()

    const resizeCalls = addEventListenerSpy.mock.calls.filter(
      ([event]) => event === 'resize',
    )
    expect(resizeCalls.length).toBeGreaterThanOrEqual(1)
    expect(typeof resizeCalls[0][1]).toBe('function')
  })

  it('should call chart.resize() on initialised charts when window is resized', async () => {
    await mountComponent()

    // At least the trend chart should have been initialised
    expect(chartInstances.length).toBeGreaterThanOrEqual(1)
    const initialised = [...chartInstances]

    // Trigger a window resize event
    window.dispatchEvent(new Event('resize'))

    // Every chart that was created must have had .resize() called
    for (const chart of initialised) {
      expect(chart.resize).toHaveBeenCalledTimes(1)
    }
  })

  it('should call chart.resize() each time the window fires a resize event', async () => {
    await mountComponent()
    expect(chartInstances.length).toBeGreaterThanOrEqual(1)
    const initialised = [...chartInstances]

    window.dispatchEvent(new Event('resize'))
    window.dispatchEvent(new Event('resize'))
    window.dispatchEvent(new Event('resize'))

    for (const chart of initialised) {
      expect(chart.resize).toHaveBeenCalledTimes(3)
    }
  })

  it('should remove the resize listener when unmounted', async () => {
    await mountComponent()

    // Capture the handler that was registered
    const resizeCalls = addEventListenerSpy.mock.calls.filter(
      ([event]) => event === 'resize',
    )
    const registeredHandler = resizeCalls[0][1]

    wrapper.unmount()

    const removeCalls = removeEventListenerSpy.mock.calls.filter(
      ([event]) => event === 'resize',
    )
    expect(removeCalls.length).toBeGreaterThanOrEqual(1)
    // The same handler function must be passed to removeEventListener
    expect(removeCalls[0][1]).toBe(registeredHandler)
  })

  it('should dispose charts and stop responding to resize after unmount', async () => {
    await mountComponent()
    expect(chartInstances.length).toBeGreaterThanOrEqual(1)
    const initialised = [...chartInstances]

    wrapper.unmount()

    // All created charts must be disposed
    for (const chart of initialised) {
      expect(chart.dispose).toHaveBeenCalled()
    }

    // Reset call counts after dispose
    for (const chart of initialised) {
      chart.resize.mockClear()
    }

    // Firing resize after unmount should not trigger chart resize
    window.dispatchEvent(new Event('resize'))

    for (const chart of initialised) {
      expect(chart.resize).not.toHaveBeenCalled()
    }
  })
})
