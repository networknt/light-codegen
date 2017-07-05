/**
 * Created by Nicholas Azar on 2017-07-01.
 */

import React, {Component} from 'react';
import {Layout} from 'antd';
import AppSteps from "./app-steps/AppSteps";
import SelectSchemaStep from "./select-schema-step/SelectSchemaStep";

const {Content} = Layout;

class AppContent extends Component {

    constructor(props) {
        super(props);
        this.state = {
            currentStep: 0
        };
    }

    render() {
        return (
            <Content className="root-content">
                <AppSteps currentStep={this.state.currentStep}/>
                <Layout className="primary-paper">
                    <Content style={{ padding: '0 24px', minHeight: 280 }}>
                        {this.state.currentStep === 0 &&
                            <SelectSchemaStep />
                        }
                    </Content>
                </Layout>
            </Content>
        )
    }
}

export default AppContent;