/**
 * Created by Nicholas Azar on 2017-07-01.
 */

import React, {Component} from 'react';
import {Layout} from 'antd';

const {Footer} = Layout;

class AppFooter extends Component {
    render() {
        return (
            <Footer style={{ textAlign: 'center' }}>
                NetworkNT
            </Footer>
        )
    }
}

export default AppFooter;