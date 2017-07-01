import React, {Component} from 'react';
import {Layout, LocaleProvider, Menu, Steps} from 'antd';
import './App.less';
import enUs from 'antd/lib/locale-provider/en_US'

const {Header, Content, Footer} = Layout;
const Step = Steps.Step;

class App extends Component {
    render() {
        return (
            <LocaleProvider locale={enUs}>
                <Layout>
                    <Header className="header">
                        <div className="logo">Light-4j</div>
                        <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['1']}>
                            <Menu.Item key="1" style={{ backgroundColor: "#E84E40", marginTop: "6px"}}>Codegen</Menu.Item>
                        </Menu>
                    </Header>
                    <Content className="root-content">
                        <Steps current={0} style={{maxWidth: "1000px", margin: "30px auto"}}>
                            <Step title="Schema" description="Some text explaining what the schema does" />
                            <Step title="Config" description="Configuration for the API" />
                            <Step title="Generate" description="Verify and Generate" />
                        </Steps>
                        <Layout className="primary-paper">
                            <Content style={{ padding: '0 24px', minHeight: 280 }}>
                                Content
                            </Content>
                        </Layout>
                    </Content>
                    <Footer style={{ textAlign: 'center' }}>
                        NetworkNT
                    </Footer>
                </Layout>
            </LocaleProvider>
        );
    }
}

export default App;
