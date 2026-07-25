// Charts Renderer - Database Transaction Presentation

class ChartRenderer {
    constructor() {
        this.charts = new Map();
        this.init();
    }
    
    init() {
        // Wait for ECharts to load
        if (typeof echarts !== 'undefined') {
            this.setupCharts();
        } else {
            window.addEventListener('load', () => this.setupCharts());
        }
    }
    
    setupCharts() {
        // Initialize charts for specific slides
        this.initIsolationComparisonChart();
        this.initPerformanceChart();
        this.initTransactionFlowChart();
    }
    
    initIsolationComparisonChart() {
        const chartElement = document.getElementById('isolation-comparison-chart');
        if (!chartElement) return;
        
        const chart = echarts.init(chartElement);
        const option = {
            title: {
                text: '隔离级别对比',
                left: 'center',
                textStyle: {
                    color: '#4A90E2',
                    fontSize: 20
                }
            },
            tooltip: {
                trigger: 'axis',
                axisPointer: {
                    type: 'shadow'
                }
            },
            legend: {
                data: ['脏读', '不可重复读', '幻读'],
                top: 40
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '3%',
                containLabel: true
            },
            xAxis: {
                type: 'category',
                data: ['READ UNCOMMITTED', 'READ COMMITTED', 'REPEATABLE READ', 'SERIALIZABLE'],
                axisLabel: {
                    interval: 0,
                    rotate: 15
                }
            },
            yAxis: {
                type: 'value',
                max: 1,
                axisLabel: {
                    formatter: function(value) {
                        return value === 1 ? '可能发生' : '已阻止';
                    }
                }
            },
            series: [
                {
                    name: '脏读',
                    type: 'bar',
                    data: [1, 0, 0, 0],
                    itemStyle: {
                        color: '#F56C6C'
                    }
                },
                {
                    name: '不可重复读',
                    type: 'bar',
                    data: [1, 1, 0, 0],
                    itemStyle: {
                        color: '#E6A23C'
                    }
                },
                {
                    name: '幻读',
                    type: 'bar',
                    data: [1, 1, 0.5, 0],
                    itemStyle: {
                        color: '#67C23A'
                    }
                }
            ]
        };
        
        chart.setOption(option);
        this.charts.set('isolation-comparison', chart);
        
        // Responsive
        window.addEventListener('resize', () => chart.resize());
    }
    
    initPerformanceChart() {
        const chartElement = document.getElementById('performance-chart');
        if (!chartElement) return;
        
        const chart = echarts.init(chartElement);
        const option = {
            title: {
                text: '隔离级别性能对比',
                left: 'center',
                textStyle: {
                    color: '#4A90E2',
                    fontSize: 20
                }
            },
            tooltip: {
                trigger: 'axis'
            },
            xAxis: {
                type: 'category',
                data: ['READ UNCOMMITTED', 'READ COMMITTED', 'REPEATABLE READ', 'SERIALIZABLE'],
                axisLabel: {
                    interval: 0,
                    rotate: 15
                }
            },
            yAxis: {
                type: 'value',
                name: '相对性能',
                axisLabel: {
                    formatter: '{value}%'
                }
            },
            series: [
                {
                    name: '并发性能',
                    type: 'line',
                    data: [100, 85, 70, 40],
                    smooth: true,
                    itemStyle: {
                        color: '#4A90E2'
                    },
                    areaStyle: {
                        color: {
                            type: 'linear',
                            x: 0,
                            y: 0,
                            x2: 0,
                            y2: 1,
                            colorStops: [
                                { offset: 0, color: 'rgba(74, 144, 226, 0.3)' },
                                { offset: 1, color: 'rgba(74, 144, 226, 0.05)' }
                            ]
                        }
                    }
                }
            ]
        };
        
        chart.setOption(option);
        this.charts.set('performance', chart);
        
        window.addEventListener('resize', () => chart.resize());
    }
    
    initTransactionFlowChart() {
        const chartElement = document.getElementById('transaction-flow-chart');
        if (!chartElement) return;
        
        const chart = echarts.init(chartElement);
        const option = {
            title: {
                text: '事务执行流程',
                left: 'center',
                textStyle: {
                    color: '#4A90E2',
                    fontSize: 20
                }
            },
            tooltip: {
                trigger: 'item',
                formatter: '{b}'
            },
            series: [
                {
                    type: 'sankey',
                    layout: 'none',
                    emphasis: {
                        focus: 'adjacency'
                    },
                    data: [
                        { name: '开始事务' },
                        { name: '执行SQL' },
                        { name: '写入Redo Log' },
                        { name: '写入Undo Log' },
                        { name: '提交事务' },
                        { name: '回滚事务' },
                        { name: '持久化' }
                    ],
                    links: [
                        { source: '开始事务', target: '执行SQL', value: 10 },
                        { source: '执行SQL', target: '写入Redo Log', value: 5 },
                        { source: '执行SQL', target: '写入Undo Log', value: 5 },
                        { source: '写入Redo Log', target: '提交事务', value: 4 },
                        { source: '写入Undo Log', target: '回滚事务', value: 1 },
                        { source: '提交事务', target: '持久化', value: 4 }
                    ],
                    lineStyle: {
                        color: 'gradient',
                        curveness: 0.5
                    },
                    itemStyle: {
                        color: '#4A90E2',
                        borderColor: '#4A90E2'
                    }
                }
            ]
        };
        
        chart.setOption(option);
        this.charts.set('transaction-flow', chart);
        
        window.addEventListener('resize', () => chart.resize());
    }
    
    // Destroy all charts
    destroy() {
        this.charts.forEach((chart) => {
            chart.dispose();
        });
        this.charts.clear();
    }
    
    // Resize all charts
    resizeAll() {
        this.charts.forEach((chart) => {
            chart.resize();
        });
    }
}

// Initialize charts when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.chartRenderer = new ChartRenderer();
    });
} else {
    window.chartRenderer = new ChartRenderer();
}
